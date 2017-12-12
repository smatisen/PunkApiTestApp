package test.punk.com.punkapitestapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadData();

    }

    private void loadData() {

        Intent intent = getIntent();

        if(intent != null){

            BeerItem beerItem = (BeerItem) intent.getSerializableExtra(CodelabUtil.BEER);
            ImageRequester imageRequester = ImageRequester.getInstance(this);

            if(beerItem != null){

                TextView title = findViewById(R.id.title);
                title.setText(beerItem.getName());

                NetworkImageView imageView = findViewById(R.id.image);
                imageRequester.setImageFromUrl(imageView, beerItem.getImage_url());

                TextView abv = findViewById(R.id.abv);
                abv.setText(String.format("ABV: %s", String.valueOf(beerItem.getAbv())));

                TextView ibu = findViewById(R.id.ibu);
                ibu.setText(String.format("IBU: %s", String.valueOf(beerItem.getIbu())));

                TextView ebc = findViewById(R.id.ebc);
                ebc.setText(String.format("EBC: %s", String.valueOf(beerItem.getEbc())));
            }
        }



    }

}
