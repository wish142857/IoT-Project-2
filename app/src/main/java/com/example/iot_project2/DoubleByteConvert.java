package com.example.iot_project2;



public class DoubleByteConvert {
    /********************
     * double转化为byte
     ********************/
    public static byte[] double2byte(double[] double_array, int double_length) {
        byte[] byte_array = new byte[2 * double_length];
        int idx = 0;
        for (final double dval: double_array) {
            final short val = (short)(dval * 32767);
            byte_array[idx++] = (byte)(val & 0x00FF);
            byte_array[idx++] = (byte)((val & 0xFF00) >>> 8);
        }
        return byte_array;
    }

    /********************
     * byte转化为double
     ********************/
    public static double[] byte2double(byte[] byte_array, int byte_length) {
        double[] double_array = new double[byte_length / 2];

        for (int i = 0; i < double_array.length; i++) {
            byte bl = byte_array[2 * i];
            byte bh = byte_array[2 * i + 1];

            short bit_mask = 0x00FF;
            short s = (short) ((bh & bit_mask) << 8 | bl & bit_mask);
            double_array[i] = s / 32768f;
        }

        return double_array;
    }

}
