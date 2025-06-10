package md.ctif.recipes_app.converters;

import com.pgvector.PGvector;

import java.util.ArrayList;
import java.util.List;

public class PGvectorListFloatConvertor {

    public static float[] floatListToArray(List<Float> list) {
        if (list == null || list.isEmpty()) {
            return new float[0];
        }
        float[] array = new float[list.size()];
        for (int i = 0; i < list.size(); i++) {
            array[i] = list.get(i);
        }
        return array;
    }

    public static List<Float> pgvectorToList(PGvector embedding) {
        if (embedding == null) {
            return List.of();
        }

        float[] floatArray = embedding.toArray();

        if (floatArray == null || floatArray.length == 0) {
            return List.of();
        }

        List<Float> floatList = new ArrayList<>(floatArray.length);
        for (float val : floatArray) {
            floatList.add(val);
        }
        return floatList;
    }
}