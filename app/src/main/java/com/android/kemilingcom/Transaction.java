package com.android.kemilingcom;

public class Transaction {
    private int id_transaksi;
    private int user_id;
    private String username_buyer;
    private int id_produk;
    private String username_seller;
    private String nama_produk;
    private int weekday_ticket;
    private int weekend_ticket;
    private int harga_produk;
    private int jumlah;
    private int total_harga;
    private String alamat;
    private String bukti_tf;
    private int validasi_pembayaran;
    private String datetime;

    // Constructor
    public Transaction() {
    }

    // Getters and Setters
    public int getId_transaksi() {
        return id_transaksi;
    }

    public void setId_transaksi(int id_transaksi) {
        this.id_transaksi = id_transaksi;
    }

    public int getUser_id() {
        return user_id;
    }

    public void setUser_id(int user_id) {
        this.user_id = user_id;
    }

    public String getUsername_buyer() {
        return username_buyer;
    }

    public void setUsername_buyer(String username_buyer) {
        this.username_buyer = username_buyer;
    }

    public int getId_produk() {
        return id_produk;
    }

    public void setId_produk(int id_produk) {
        this.id_produk = id_produk;
    }

    public String getUsername_seller() {
        return username_seller;
    }

    public void setUsername_seller(String username_seller) {
        this.username_seller = username_seller;
    }

    public String getNama_produk() {
        return nama_produk;
    }

    public void setNama_produk(String nama_produk) {
        this.nama_produk = nama_produk;
    }

    public int getWeekday_ticket() {
        return weekday_ticket;
    }

    public void setWeekday_ticket(int weekday_ticket) {
        this.weekday_ticket = weekday_ticket;
    }

    public int getWeekend_ticket() {
        return weekend_ticket;
    }

    public void setWeekend_ticket(int weekend_ticket) {
        this.weekend_ticket = weekend_ticket;
    }

    public int getHarga_produk() {
        return harga_produk;
    }

    public void setHarga_produk(int harga_produk) {
        this.harga_produk = harga_produk;
    }

    public int getJumlah() {
        return jumlah;
    }

    public void setJumlah(int jumlah) {
        this.jumlah = jumlah;
    }

    public int getTotal_harga() {
        return total_harga;
    }

    public void setTotal_harga(int total_harga) {
        this.total_harga = total_harga;
    }

    public String getAlamat() {
        return alamat;
    }

    public void setAlamat(String alamat) {
        this.alamat = alamat;
    }

    public String getBukti_tf() {
        return bukti_tf;
    }

    public void setBukti_tf(String bukti_tf) {
        this.bukti_tf = bukti_tf;
    }

    public int getValidasi_pembayaran() {
        return validasi_pembayaran;
    }

    public void setValidasi_pembayaran(int validasi_pembayaran) {
        this.validasi_pembayaran = validasi_pembayaran;
    }

    public String getDatetime() {
        return datetime;
    }

    public void setDatetime(String datetime) {
        this.datetime = datetime;
    }
}
