package com.itek.retail.database;

import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.itek.retail.model.SerialDetails;

import java.util.List;

@androidx.room.Dao
public interface SerialDetailsDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<SerialDetails> serialDetails);

    @Query("SELECT serial_no FROM serial_details WHERE epc = :epc")
    String getSerialFromEpc(String epc);

    @Query("DELETE FROM serial_details")
    void deleteAllSerialDetails();


}
