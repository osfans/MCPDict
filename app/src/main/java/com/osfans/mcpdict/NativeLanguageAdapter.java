package com.osfans.mcpdict;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import com.osfans.mcpdict.Adapter.LanguageAdapter;
/** Background cursor search with a bounded candidate window. */
public class NativeLanguageAdapter extends LanguageAdapter {
    public NativeLanguageAdapter(Context context){super(context);}
    @Override public void bindView(android.view.View view,Context context,Cursor cursor){super.bindView(view,context,cursor);com.osfans.mcpdict.Util.FontUtil.setTypeface((android.widget.TextView)view);}
    @Override public Cursor runQueryOnBackgroundThread(CharSequence query) {
        MatrixCursor result=new MatrixCursor(new String[]{"語言","_id"});
        Cursor source=DB.getLanguageCursor(query==null?"":query,"");
        if(source!=null)try {for(int i=0;i<80&&source.moveToPosition(i);i++)result.addRow(new Object[]{source.getString(0),i});}finally{source.close();}
        return result;
    }
}
