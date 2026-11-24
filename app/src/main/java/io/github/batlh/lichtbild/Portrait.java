package io.github.batlh.lichtbild;

import android.graphics.*;
import com.google.mlkit.vision.face.*;
import com.google.mlkit.vision.segmentation.SegmentationMask;
import java.nio.FloatBuffer;
import java.util.List;

final class Portrait {
    static final int W=700, H=900;
    static final class Advice extends Exception { Advice(String message){ super(message); } }
    static void require(boolean ok,String hint) throws Advice { if(!ok) throw new Advice(hint); }
    static PointF landmark(Face f,int type) throws Advice {
        FaceLandmark point=f.getLandmark(type);
        require(point!=null,"Augen und Mund vollständig zeigen");
        return point.getPosition();
    }
    static PointF middle(Face f,int type) throws Advice {
        FaceContour contour=f.getContour(type);
        require(contour!=null && !contour.getPoints().isEmpty(),"Mund gut sichtbar halten");
        List<PointF> points=contour.getPoints();
        return points.get(points.size()/2);
    }
    static Matrix align(Bitmap source, Face f) throws Advice {
        require(Math.abs(f.getHeadEulerAngleY())<9 && Math.abs(f.getHeadEulerAngleX())<9,
            "Gesicht gerade zur Kamera drehen");
        PointF left=landmark(f,FaceLandmark.LEFT_EYE), right=landmark(f,FaceLandmark.RIGHT_EYE);
        if(left.x>right.x){PointF swap=left; left=right; right=swap;}
        float dx=right.x-left.x, dy=right.y-left.y;
        float distance=(float)Math.hypot(dx,dy);
        float angle=(float)Math.toDegrees(Math.atan2(dy,dx));
        require(Math.abs(angle)<9,"Kopf aufrecht halten");
        require(distance>=52,"Etwas näher an die Kamera");
        require(f.getLeftEyeOpenProbability()!=null && f.getRightEyeOpenProbability()!=null &&
            Math.min(f.getLeftEyeOpenProbability(),f.getRightEyeOpenProbability())>.72,"Beide Augen öffnen");
        require(f.getSmilingProbability()!=null && f.getSmilingProbability()<.45,"Neutral in die Kamera schauen");
        PointF upper=middle(f,FaceContour.UPPER_LIP_BOTTOM), lower=middle(f,FaceContour.LOWER_LIP_TOP);
        require(Math.abs(lower.y-upper.y)<distance*.075,"Lippen locker schließen");
        float x=(right.x+left.x)/2, y=(right.y+left.y)/2;
        FaceContour outline=f.getContour(FaceContour.FACE);
        require(outline!=null,"Gesicht vollständig zeigen");
        float chin=0;
        for(PointF p:outline.getPoints()) chin=Math.max(chin,((p.y-y)*dx-(p.x-x)*dy)/distance);
        require(chin>distance*.8,"Kinn vollständig zeigen");
        float scale=360/chin;
        Matrix m=new Matrix();
        m.postTranslate(-x,-y); m.postRotate(-angle); m.postScale(scale,scale); m.postTranslate(350,390);
        Matrix inverse=new Matrix(); m.invert(inverse);
        float[] corners={0,0,W,0,W,H,0,H}; inverse.mapPoints(corners);
        for(int i=0;i<8;i+=2) require(corners[i]>=0 && corners[i]<source.getWidth() &&
            corners[i+1]>=0 && corners[i+1]<source.getHeight(),"Mehr Abstand halten und Gesicht mittig platzieren");
        return m;
    }
    static int light(int c){ return (77*Color.red(c)+150*Color.green(c)+29*Color.blue(c))>>8; }
    static void checkLight(Bitmap b, Face f) throws Advice {
        Rect r=f.getBoundingBox();
        int l=Math.max(2,r.left+r.width()/4), t=Math.max(2,r.top+r.height()/3);
        int endX=Math.min(b.getWidth()-2,r.right-r.width()/4), endY=Math.min(b.getHeight()-2,r.bottom-r.height()/5);
        long sum=0,edge=0,clipped=0; int n=0;
        for(int y=t;y<endY;y+=3) for(int x=l;x<endX;x+=3){
            int v=light(b.getPixel(x,y)); sum+=v; n++;
            if(v<12 || v>246) clipped++;
            int lap=4*v-light(b.getPixel(x-1,y))-light(b.getPixel(x+1,y))
                -light(b.getPixel(x,y-1))-light(b.getPixel(x,y+1));
            edge+=(long)lap*lap;
        }
        require(n>0 && sum/n>38 && sum/n<228 && clipped<.14*n,"Gesicht gleichmäßig von vorne beleuchten");
        require(edge/n>16,"Ruhig halten und Kamera scharfstellen lassen");
    }
    static Bitmap finish(Bitmap src, Matrix transform, SegmentationMask mask) throws Advice {
        int w=mask.getWidth(), h=mask.getHeight();
        require(w==src.getWidth() && h==src.getHeight(),"Freistellung erneut versuchen");
        FloatBuffer confidence=mask.getBuffer().asFloatBuffer();
        int[] row=new int[w];
        Bitmap cut=Bitmap.createBitmap(w,h,Bitmap.Config.ARGB_8888);
        for(int y=0;y<h;y++){
            src.getPixels(row,0,w,0,y,w,1);
            for(int x=0;x<w;x++){
                float a=Math.max(0,Math.min(1,(confidence.get(y*w+x)-.12f)/.76f));
                int c=row[x];
                row[x]=Color.rgb(Math.round(224+(Color.red(c)-224)*a),
                    Math.round(224+(Color.green(c)-224)*a),Math.round(224+(Color.blue(c)-224)*a));
            }
            cut.setPixels(row,0,w,0,y,w,1);
        }
        Matrix inv=new Matrix(); transform.invert(inv);
        int crown=-1;
        for(int y=0;y<350;y+=3){
            int hits=0;
            for(int x=275;x<=425;x+=15){
                float[] p={x,y}; inv.mapPoints(p);
                int px=Math.min(w-1,Math.max(0,(int)p[0])),py=Math.min(h-1,Math.max(0,(int)p[1]));
                if(confidence.get(py*w+px)>.7) hits++;
            }
            if(hits>=3){crown=y;break;}
        }
        require(crown>=12 && crown<175,"Scheitelkontur unklar. Haare vollständig zeigen, Hintergrund vereinfachen");
        // 20 px/mm. Scale the estimated crown-to-chin span to 34 mm.
        float correction=680f/(750-crown);
        transform.postScale(correction,correction,350,390);
        Matrix finalInverse=new Matrix(); transform.invert(finalInverse);
        float[] bounds={0,0,W,0,W,H,0,H}; finalInverse.mapPoints(bounds);
        for(int i=0;i<bounds.length;i+=2) require(bounds[i]>=0 && bounds[i]<w &&
            bounds[i+1]>=0 && bounds[i+1]<h,"Etwas zurücktreten, damit der Ausschnitt vollständig ist");
        Bitmap result=Bitmap.createBitmap(W,H,Bitmap.Config.ARGB_8888);
        Canvas canvas=new Canvas(result); canvas.drawColor(Color.rgb(224,224,224));
        canvas.drawBitmap(cut,transform,new Paint(Paint.FILTER_BITMAP_FLAG)); cut.recycle();
        return result;
    }
}
