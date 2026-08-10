package pip;


public class LinkedList{
    //Entry Object[3] [0]: element, [1]: next, [2]: previous
    private transient Object[] header = new Object[]{
                    null, null, null
    };
    private transient int size = 0;

    private static final int ELEMENT = 0;
    private static final int NEXT = 1;
    private static final int PREVIOUS = 2;

    private Object getEntry(Object[] entry, int first, int second){
        return ((Object[])entry[first])[second];
    }

    private Object getEntry(Object[] entry, int first){
        return entry[first];
    }

    private void setEntry(Object[] entry, Object obj, int first, int second){
        ((Object[])entry[first])[second] = obj;
    }

    private void setEntry(Object[] entry, Object obj, int first){
        entry[first] = obj;
    }

    public LinkedList(){
        setEntry(header, header, NEXT);
        setEntry(header, header, PREVIOUS);
    }

    public Object getFirst(){
        if(size == 0){
            throw new IndexOutOfBoundsException();
        }

        return getEntry(header, NEXT, ELEMENT);
    }

    public Object getLast(){
        if(size == 0){
            throw new IndexOutOfBoundsException();
        }

        return getEntry(header, PREVIOUS, ELEMENT);
    }

    public Object removeFirst(){
        Object first = getEntry(header, NEXT, ELEMENT);
        remove((Object[])getEntry(header, NEXT));

        return first;
    }

    public Object removeLast(){
        Object last = getEntry(header, PREVIOUS, ELEMENT);
        remove((Object[])getEntry(header, PREVIOUS));

        return last;
    }

    public void addFirst(Object o){
        addBefore(o, (Object[])getEntry(header, NEXT));
    }

    public void addLast(Object o){
        addBefore(o, header);
    }

    public int size(){
        return size;
    }

    public boolean add(Object o){
        addBefore(o, header);

        return true;

    }

    public boolean remove(Object o){
        if(o == null){
            for(Object[] e = (Object[])getEntry(header, NEXT); e != header; e = (Object[])getEntry(e, NEXT)){
                if(getEntry(e, ELEMENT) == null){
                    remove(e);

                    return true;
                }
            }
        }else{
            for(Object[] e = (Object[])getEntry(header, NEXT); e != header; e = (Object[])getEntry(e, NEXT)){
                if(o.equals(getEntry(e, ELEMENT))){
                    remove(e);

                    return true;
                }
            }
        }

        return false;
    }

    public void clear(){
        setEntry(header, header, NEXT);
        setEntry(header, header, PREVIOUS);
        size = 0;
    }

    public Object get(int index){
        return getEntry(entry(index), ELEMENT);
    }

    public Object set(int index, Object element){
        Object[] e = entry(index);
        Object oldVal = getEntry(e, ELEMENT);
        setEntry(e, element, ELEMENT);

        return oldVal;
    }

    public void add(int index, Object element){
        addBefore(element, (index == size? header: entry(index)));
    }

    public Object remove(int index){
        Object[] e = entry(index);
        remove(e);

        return getEntry(e, ELEMENT);
    }

    private Object[] entry(int index){
        if(index < 0 || index >= size){
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }

        Object[] e = header;

        if(index < (size >> 1)){
            for(int i = 0; i <= index; i++){
                e = (Object[])getEntry(e, NEXT);
            }
        }else{
            for(int i = size; i > index; i--){
                e = (Object[])getEntry(e, PREVIOUS);
            }
        }

        return e;
    }

    private Object[] addBefore(Object o, Object[] e){
        Object[] newEntry = new Object[]{
                        o, e, (Object[])getEntry(e, PREVIOUS)
        };
        setEntry(newEntry, newEntry, PREVIOUS, NEXT);
        setEntry(newEntry, newEntry, NEXT, PREVIOUS);
        size++;

        return newEntry;
    }

    private void remove(Object[] e){
        if(e == header){
            throw new IndexOutOfBoundsException();
        }

        setEntry(e, (Object[])getEntry(e, NEXT), PREVIOUS, NEXT);
        setEntry(e, (Object[])getEntry(e, PREVIOUS), NEXT, PREVIOUS);
        size--;
    }
}
