package io.anuke.mindustry.editor;

import arc.struct.Seq;

public class OperationStack{
    private final static int maxSize = 10;
    private Seq<DrawOperation> stack = new Seq<>();
    private int index = 0;

    public OperationStack(){

    }

    public void clear(){
        stack.clear();
        index = 0;
    }

    public void add(DrawOperation action){
        stack.truncate(stack.size + index);
        index = 0;
        stack.add(action);

        if(stack.size > maxSize){
            stack.removeIndex(0);
        }
    }

    public boolean canUndo(){
        return index < stack.size - 1;
    }

    public boolean canRedo(){
        return index >= 0;
    }

    public void undo(MapEditor editor){
        if(!canUndo()) return;

        int undoIndex = stack.size - 1 + index;
        stack.get(undoIndex).undo(editor);
        index--;
    }

    public void redo(MapEditor editor){
        if(!canRedo()) return;

        index++;
        int redoIndex = stack.size - 1 + index;
        stack.get(redoIndex).redo(editor);
    }
}
