package io.github.batlh.lichtbild;

import android.Manifest;
import android.content.*;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.net.Uri;
import android.os.*;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;
import androidx.activity.ComponentActivity;
import androidx.camera.core.*;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.*;
import com.google.mlkit.vision.segmentation.*;
import com.google.mlkit.vision.segmentation.selfie.SelfieSegmenterOptions;
import java.io.*;
import java.util.List;
import java.util.concurrent.*;

public class Studio extends ComponentActivity {
    final ExecutorService worker=Executors.newSingleThreadExecutor();
    final FaceDetector counter=FaceDetection.getClient(new FaceDetectorOptions.Builder().setMinFaceSize(.08f).build());
    final FaceDetector detector=FaceDetection.getClient(new FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL).build());
    final Segmenter segmenter=Segmentation.getClient(new SelfieSegmenterOptions.Builder()
        .setDetectorMode(SelfieSegmenterOptions.SINGLE_IMAGE_MODE).build());
    final String[] names={"Gesundheitskarte","Führerschein","Personalausweis · Probe"};
    LinearLayout root; TextView status; PreviewView preview;
    Button primary,secondary; ProcessCameraProvider cameras;
    int profile; boolean front=true; volatile boolean armed; volatile int generation;
    int dp(int n){return Math.round(n*getResources().getDisplayMetrics().density);}
    @Override public void onCreate(Bundle state){super.onCreate(state);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SECURE); home();}
    void layout(){
        root=new LinearLayout(this); root.setOrientation(1); root.setPadding(dp(20),dp(16),dp(20),dp(12));
        root.setBackgroundColor(0xff122021); setContentView(root);
    }
    TextView label(String value,int size){
        TextView t=new TextView(this); t.setText(value); t.setTextSize(size); t.setTextColor(0xffeff7f4);
        t.setPadding(0,dp(6),0,dp(8)); root.addView(t); return t;
    }
    Button button(String value,Runnable action){
        Button b=new Button(this); b.setAllCaps(false); b.setText(value);
        b.setOnClickListener(v->action.run()); root.addView(b); return b;
    }
    void stopCamera(){ generation++; armed=false; if(cameras!=null)cameras.unbindAll(); }
    void home(){
        stopCamera(); layout(); label("Lichtbild",34); label("Wofür brauchst du dein Foto?",20);
        for(int i=0;i<names.length;i++){ final int index=i; button(names[i],()->{profile=index;cameraPage();});}
        label("Die Prüfung läuft auf deinem Gerät. Erst wenn du das fertige Foto speicherst, wird eine JPEG-Datei angelegt.",16);
    }
    void cameraPage(){
        stopCamera(); layout(); label(names[profile],24);
        label(profile==2?"Für den Antrag erfolgt die Aufnahme bei der Behörde oder einem registrierten Fotodienstleister.":
            profile==1?"35 × 45 mm · Beim Drucken die tatsächliche Größe wählen.":"Frontal in die Linse schauen. Schultern gerade halten.",15);
        preview=new PreviewView(this); preview.setScaleType(PreviewView.ScaleType.FIT_CENTER);
        root.addView(preview,new LinearLayout.LayoutParams(-1,0,1));
        status=label("Kamera wird vorbereitet …",17);
        primary=button("Automatische Aufnahme starten",()->{armed=true;primary.setEnabled(false);status.setText("Prüfe das Kamerabild …");});
        primary.setEnabled(false);
        secondary=button("Kamera wechseln",()->{front=!front;cameraPage();});
        if(checkSelfPermission(Manifest.permission.CAMERA)!=PackageManager.PERMISSION_GRANTED)
            requestPermissions(new String[]{Manifest.permission.CAMERA},7);
        else bind();
    }
    @Override public void onRequestPermissionsResult(int code,String[] permissions,int[] results){
        super.onRequestPermissionsResult(code,permissions,results);
        if(code!=7)return;
        if(results.length>0 && results[0]==PackageManager.PERMISSION_GRANTED) bind();
        else {status.setText("Kamerazugriff erforderlich. Du kannst ihn in den App-Einstellungen erlauben.");}
    }
    void bind(){
        int token=generation;
        var future=ProcessCameraProvider.getInstance(this);
        future.addListener(()->{
            if(token!=generation)return;
            try {
                cameras=future.get();
                CameraSelector selector=front?CameraSelector.DEFAULT_FRONT_CAMERA:CameraSelector.DEFAULT_BACK_CAMERA;
                if(!cameras.hasCamera(selector)){front=!front; selector=front?CameraSelector.DEFAULT_FRONT_CAMERA:CameraSelector.DEFAULT_BACK_CAMERA;}
                Preview live=new Preview.Builder().build(); live.setSurfaceProvider(preview.getSurfaceProvider());
                ImageAnalysis analysis=new ImageAnalysis.Builder().setTargetResolution(new android.util.Size(1440,1080))
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST).build();
                analysis.setAnalyzer(worker,new Examiner(token)); cameras.bindToLifecycle(this,selector,live,analysis);
                primary.setEnabled(true); status.setText("Gesicht mittig platzieren und Aufnahme starten.");
            }catch(Exception e){status.setText("Kamera konnte nicht geöffnet werden. Kamera wechseln oder erneut versuchen.");}
        },getMainExecutor());
    }
    final class Examiner implements ImageAnalysis.Analyzer {
        final int token; int valid; long since;
        Examiner(int token){this.token=token;}
        void hint(String s){runOnUiThread(()->{if(token==generation)status.setText(s);});}
        @Override public void analyze(ImageProxy frame){
            try {
                if(token!=generation || !armed)return;
                Matrix orientation=new Matrix(); orientation.postRotate(frame.getImageInfo().getRotationDegrees());
                Bitmap raw=frame.toBitmap();
                Bitmap b=Bitmap.createBitmap(raw,0,0,raw.getWidth(),raw.getHeight(),orientation,true);
                InputImage input=InputImage.fromBitmap(b,0);
                var people=Tasks.await(counter.process(input));
                Portrait.require(people.size()==1,people.isEmpty()?"Gesicht mittig ins Bild nehmen":"Nur eine Person im Bild lassen");
                List<Face> faces=Tasks.await(detector.process(input));
                Portrait.require(faces.size()==1,"Gesicht und Augen vollständig zeigen");
                Face face=faces.get(0);
                Matrix crop=Portrait.align(b,face); Portrait.checkLight(b,face);
                if(valid++==0)since=SystemClock.elapsedRealtime();
                if(valid<3 || SystemClock.elapsedRealtime()-since<800){hint("Position halten …");return;}
                Bitmap result=Portrait.finish(b,crop,Tasks.await(segmenter.process(input)));
                if(token!=generation || !armed)return;
                armed=false;
                runOnUiThread(()->{if(token==generation)review(result);});
            }catch(Portrait.Advice e){valid=0;hint(e.getMessage());}
            catch(Exception e){valid=0;armed=false;runOnUiThread(()->{if(token==generation){status.setText("Bildanalyse unterbrochen. Bitte erneut starten.");primary.setEnabled(true);}});}
            finally {frame.close();}
        }
    }
    void review(Bitmap result){
        stopCamera(); layout(); label("Foto prüfen",26);
        ImageView photo=new ImageView(this); photo.setImageBitmap(result); photo.setContentDescription("Ausgerichtetes Lichtbild");
        root.addView(photo,new LinearLayout.LayoutParams(-1,0,1));
        status=label("Sind Haarkanten und Schultern vollständig? Augen sichtbar, Blick gerade, keine Reflexe oder Schatten?",16);
        primary=button("Bestätigen und lokal speichern",()->save(result));
        secondary=button("Neu aufnehmen",this::cameraPage);
    }
    void save(Bitmap result){
        primary.setEnabled(false); secondary.setEnabled(false);
        int token=generation;
        String name="Lichtbild-"+new String[]{"Gesundheitskarte","Fuehrerschein","Probe"}[profile]+"-"+System.currentTimeMillis()+".jpg";
        worker.execute(()->{
            Uri uri=null;
            try{
                ContentValues v=new ContentValues(); v.put(MediaStore.MediaColumns.DISPLAY_NAME,name);
                v.put(MediaStore.MediaColumns.MIME_TYPE,"image/jpeg");
                v.put(MediaStore.MediaColumns.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS+"/Lichtbild");
                v.put(MediaStore.MediaColumns.IS_PENDING,1);
                uri=getContentResolver().insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,v);
                if(uri==null)throw new IOException();
                try(OutputStream out=getContentResolver().openOutputStream(uri)){
                    if(out==null || !result.compress(Bitmap.CompressFormat.JPEG,95,out))throw new IOException();
                }
                try (ParcelFileDescriptor fd=getContentResolver().openFileDescriptor(uri,"rw")){
                    if(fd==null) throw new IOException();
                    android.media.ExifInterface exif=new android.media.ExifInterface(fd.getFileDescriptor());
                    exif.setAttribute(android.media.ExifInterface.TAG_RESOLUTION_UNIT,"2");
                    exif.setAttribute(android.media.ExifInterface.TAG_X_RESOLUTION,"508/1");
                    exif.setAttribute(android.media.ExifInterface.TAG_Y_RESOLUTION,"508/1");
                    exif.saveAttributes();
                }
                v.clear();v.put(MediaStore.MediaColumns.IS_PENDING,0);
                if(getContentResolver().update(uri,v,null,null)!=1)throw new IOException();
                runOnUiThread(()->{if(token==generation){status.setText("Gespeichert in Downloads/Lichtbild/"+name);secondary.setEnabled(true);primary.setVisibility(View.GONE);}});
            }catch(Exception e){
                if(uri!=null)getContentResolver().delete(uri,null,null);
                runOnUiThread(()->{if(token==generation){status.setText("Speichern fehlgeschlagen. Freien Speicher prüfen.");primary.setEnabled(true);secondary.setEnabled(true);}});
            }
        });
    }
    @Override public void onBackPressed(){if(preview!=null){preview=null;home();}else super.onBackPressed();}
    @Override protected void onStop(){super.onStop(); if(armed){armed=false; if(primary!=null)primary.setEnabled(true);}}
    @Override protected void onDestroy(){stopCamera();worker.execute(()->{counter.close();detector.close();segmenter.close();});worker.shutdown();super.onDestroy();}
}
