package com.itek.retail.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;

@Entity(
        tableName = "serial_details",
        indices = {@Index(value = {"ean", "epc"}, unique = true)}
)

public class SerialDetails implements Serializable, Parcelable {

    public SerialDetails() {/*Empty constructor*/}

    public static final Creator<SerialDetails> CREATOR = new Creator<SerialDetails>() {
        @Override
        public SerialDetails createFromParcel(Parcel in) {
            return new SerialDetails(in);
        }

        @Override
        public SerialDetails[] newArray(int size) {
            return new SerialDetails[size];
        }
    };

    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    public Integer id;

    @SerializedName(value = "skus", alternate = {"EAN", "Ean", "ean", "EANS", "EANs", "Eans", "eans", "BarCode", "Barcode", "barCode", "barcode", "bar_code", "BarCodes", "Barcodes", "barCodes", "barcodes", "bar_codes", "SkuId", "Skuid", "skuid", "skuId", "skuID", "sku_id", "SkuIds", "Skuids", "skuids", "skuIds", "skuIDs", "sku_ids", "SKUS", "Skus", "SKU", "Sku", "sku", "isbn", "Isbn", "ISBN", "isbns", "Isbns", "ISBNS"})
    @ColumnInfo(name = "ean", defaultValue = "")
    public String ean = "";

    @ColumnInfo(name = "epc", defaultValue = "")
    public String epc = "";

    @SerializedName(value = "serialNo", alternate = {"serialno", "serial_no", "SerialNo"})
    @ColumnInfo(name = "serial_no", defaultValue = "")
    public String serialNo = "";

    @Override
    public int describeContents() {
        return 0;
    }

    private SerialDetails(Parcel in) {
        if (in.readByte() == 0) {
            id = null;
        } else {
            id = in.readInt();
        }
        ean = in.readString();
        epc = in.readString();
        serialNo = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        if (id == null) {
            dest.writeByte((byte) 0);
        } else {
            dest.writeByte((byte) 1);
            dest.writeInt(id);
        }
        dest.writeString(ean);
        dest.writeString(epc);
        dest.writeString(serialNo);
    }
}
