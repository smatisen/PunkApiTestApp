package test.punk.com.punkapitestapp;


import java.io.Serializable;

public class BeerItem implements Serializable {


    private String id, name, image_url;
    private double abv, ibu, ebc;
    boolean isFavorite;

    public BeerItem() {
    }



    public BeerItem(String id, String name, String image_url, double abv, double ibu, double ebc, boolean isFavorite) {
        this.id = id;
        this.name = name;
        this.image_url = image_url;
        this.abv = abv;
        this.ibu = ibu;
        this.ebc = ebc;
        this.isFavorite = isFavorite;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getImage_url() {
        return image_url;
    }

    public void setImage_url(String image_url) {
        this.image_url = image_url;
    }

    public double getAbv() {
        return abv;
    }

    public void setAbv(double abv) {
        this.abv = abv;
    }

    public double getIbu() {
        return ibu;
    }

    public void setIbu(double ibu) {
        this.ibu = ibu;
    }

    public double getEbc() {
        return ebc;
    }

    public void setEbc(double ebc) {
        this.ebc = ebc;
    }
}


