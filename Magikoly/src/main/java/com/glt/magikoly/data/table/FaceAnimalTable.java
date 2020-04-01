package com.glt.magikoly.data.table;

import com.glt.magikoly.data.SqlGenerator;

public interface FaceAnimalTable {
    String TABLE_NAME = "face_animal";
    String ID = "_id";
    String FACE_FEATURE = "face_feature";
    String GENDER = "gender";
    String ETHNICITY = "ethnicity";
    String ANIMAL = "animal";

    String CREATE_SQL = SqlGenerator.create(TABLE_NAME)
            .addColumn(ID, SqlGenerator.INTEGER, true, true, true, true)
            .addColumn(FACE_FEATURE, SqlGenerator.TEXT, false, false, true, true)
            .addColumn(GENDER, SqlGenerator.TEXT)
            .addColumn(ETHNICITY, SqlGenerator.INTEGER)
            .addColumn(ANIMAL, SqlGenerator.TEXT)
            .generate();

    class FaceAnimalBean {
        public float[] faceFeature;
        public String gender;
        public int ethnicity;
        public String animal;
    }
}


