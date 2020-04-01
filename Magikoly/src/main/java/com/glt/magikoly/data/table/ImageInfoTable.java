package com.glt.magikoly.data.table;

import com.glt.magikoly.data.SqlGenerator;

public interface ImageInfoTable {
    String TABLE_NAME = "image_info";
    String IMG_ID = "img_id";
    String FACE_COUNT = "face_count";
    String FACE_AREA_PERCENT = "face_area_percent";
    String IS_VALID = "is_valid";

    String CREATE_SQL = SqlGenerator.create(TABLE_NAME)
            .addColumn(IMG_ID, SqlGenerator.INTEGER, true, false, true, true)
            .addColumn(FACE_COUNT, SqlGenerator.INTEGER)
            .addColumn(FACE_AREA_PERCENT, SqlGenerator.NUMERIC)
            .addColumn(IS_VALID, SqlGenerator.INTEGER)
            .generate();
}
