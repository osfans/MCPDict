
package android.database;
public interface Cursor extends AutoCloseable {
    boolean isAfterLast();
    boolean moveToFirst();
    boolean moveToNext();
    int getColumnIndex(String name);
    String getString(int index);
    String[] getColumnNames();
    int getPosition();
    boolean moveToPosition(int position);
    boolean isClosed();
    void close();
}
