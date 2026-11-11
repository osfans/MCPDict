package com.osfans.mcpdict;
import android.content.Context;
import android.graphics.*;
import android.view.*;
import java.util.*;
import java.util.function.Consumer;
/** Stacked flow bands with conserved node weights. Full scrollable graph; vector hit tests keep memory bounded. */
public class SankeyView extends View {
    private final List<PhonologyActivity.Group> flows;
    private final Consumer<PhonologyActivity.Group> click;
    private final android.text.TextPaint paint=new android.text.TextPaint(3);
    private final List<float[]> hit=new ArrayList<>();
    @Override protected void onMeasure(int width,int height){java.util.Set<String> a=new java.util.HashSet<>(),b=new java.util.HashSet<>();for(PhonologyActivity.Group g:flows){a.add(g.left);b.add(g.right);}float density=getResources().getDisplayMetrics().density;int target=Math.round(Math.max(360,Math.max(Math.max(a.size(),b.size())*36,flows.size()*4))*density);setMeasuredDimension(MeasureSpec.getSize(width),target);}
    public SankeyView(Context context,List<PhonologyActivity.Group> source,Consumer<PhonologyActivity.Group> click){super(context);flows=new ArrayList<>(source);this.click=click;setContentDescription("共同字音類桑基圖；點擊流線查看字音；上下捲動查看全部");android.widget.TextView font=new android.widget.TextView(context);com.osfans.mcpdict.Util.FontUtil.setTypeface(font);paint.setTypeface(font.getTypeface());}
    @Override protected void onDraw(Canvas canvas){
        super.onDraw(canvas);hit.clear();int start=0,end=flows.size();if(start>=end)return;
        float density=getResources().getDisplayMetrics().density,w=getWidth(),h=getHeight()-12*density,gap=6*density;
        Map<String,Integer> left=new LinkedHashMap<>(),right=new LinkedHashMap<>();int total=0;
        for(int i=start;i<end;i++){PhonologyActivity.Group g=flows.get(i);int weight=g.characters.size();left.merge(g.left,weight,Integer::sum);right.merge(g.right,weight,Integer::sum);total+=weight;}
        float scale=Math.max(1,h-gap*Math.max(left.size(),right.size()))/Math.max(1,total);
        Map<String,Float> ly=positions(left,scale,gap),ry=positions(right,scale,gap),lo=new HashMap<>(),ro=new HashMap<>();
        for(int i=start;i<end;i++){
            PhonologyActivity.Group g=flows.get(i);float thickness=g.characters.size()*scale;
            float a=ly.get(g.left)+lo.getOrDefault(g.left,0f),b=ry.get(g.right)+ro.getOrDefault(g.right,0f);
            lo.put(g.left,lo.getOrDefault(g.left,0f)+thickness);ro.put(g.right,ro.getOrDefault(g.right,0f)+thickness);
            Path path=new Path();path.moveTo(w*.24f,a);path.cubicTo(w*.46f,a,w*.54f,b,w*.74f,b);path.lineTo(w*.74f,b+thickness);path.cubicTo(w*.54f,b+thickness,w*.46f,a+thickness,w*.24f,a+thickness);path.close();
            paint.setColor(Color.HSVToColor(145,new float[]{(i*57)%360,.65f,.75f}));canvas.drawPath(path,paint);hit.add(new float[]{a,b,thickness});
        }
        paint.setColor(com.osfans.mcpdict.Util.App.obtainColor(getContext(),android.R.attr.textColorPrimary));paint.setTextSize(13*getResources().getDisplayMetrics().scaledDensity);
        for(String key:left.keySet())canvas.drawText(shorten(key,w*.23f),2,ly.get(key)+left.get(key)*scale/2+paint.getTextSize()/3,paint);
        for(String key:right.keySet())canvas.drawText(shorten(key,w*.24f),w*.76f,ry.get(key)+right.get(key)*scale/2+paint.getTextSize()/3,paint);
    }
    private String shorten(String text,float width){return android.text.TextUtils.ellipsize(text,paint,width,android.text.TextUtils.TruncateAt.END).toString();}
    private Map<String,Float> positions(Map<String,Integer> weights,float scale,float gap){Map<String,Float> values=new HashMap<>();float y=0;for(String key:weights.keySet()){values.put(key,y);y+=weights.get(key)*scale+gap;}return values;}
    @Override public boolean onTouchEvent(MotionEvent event){if(event.getAction()!=MotionEvent.ACTION_UP)return true;performClick();java.util.List<PhonologyActivity.Group> matches=new java.util.ArrayList<>();for(int i=0;i<hit.size();i++)if(SankeyHit.contains(getWidth(),hit.get(i)[0],hit.get(i)[1],hit.get(i)[2],event.getX(),event.getY(),5*getResources().getDisplayMetrics().density))matches.add(flows.get(i));if(matches.size()==1)click.accept(matches.get(0));else if(!matches.isEmpty()){String[] options=new String[matches.size()];for(int i=0;i<options.length;i++){PhonologyActivity.Group g=matches.get(i);options[i]=g.left+" → "+g.right+"（"+g.characters.size()+" 字）";}new androidx.appcompat.app.AlertDialog.Builder(getContext()).setTitle("選擇重疊流帶").setItems(options,(d,i)->click.accept(matches.get(i))).show();}return true;}
    @Override public boolean performClick(){super.performClick();return true;}
}
