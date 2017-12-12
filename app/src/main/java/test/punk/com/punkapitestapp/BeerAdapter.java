/**
 * Copyright Google Inc. All Rights Reserved.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package test.punk.com.punkapitestapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.util.SortedList;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.volley.toolbox.NetworkImageView;

import java.util.List;

public class BeerAdapter extends RecyclerView.Adapter<BeerAdapter.ViewHolder> {

    private static final String TAG = "TaskAdapter";

    private final ImageRequester imageRequester;
    private Context mContext;

    private SortedList<BeerItem> mList;

    public void refresh() {
        notifyDataSetChanged();

    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView mLabelTextView;
        private final NetworkImageView imageView;
        private ImageButton mImageButton;

        public ViewHolder(View v) {
            super(v);
            mLabelTextView = v.findViewById(R.id.label);
            imageView = itemView.findViewById(R.id.image);
            mImageButton = itemView.findViewById(R.id.favorite_button);

            itemView.setOnClickListener(clickListener);
            mImageButton.setOnClickListener(clickListener1);

        }

        private final View.OnClickListener clickListener1 = new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                BeerItem beer = (BeerItem) view.getTag(R.id.tag_product_entry);
                updateBeerItemStatus(beer.getId());

                Log.d("XXXXX", "onClick: " + beer.isFavorite());

            }
        };

        private final View.OnClickListener clickListener =
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        BeerItem beer = (BeerItem) v.getTag(R.id.tag_product_entry);

                        Intent detailsIntent = new Intent(mContext, DetailActivity.class);
                        Bundle bundle = new Bundle();
                        bundle.putSerializable(CodelabUtil.BEER, beer);
                        detailsIntent.putExtras(bundle);
                        mContext.startActivity(detailsIntent);
                    }
                };

        public void bind(BeerItem beerItem, ImageRequester imageRequester) {

            mLabelTextView.setText(beerItem.getName());
            imageRequester.setImageFromUrl(imageView, beerItem.getImage_url());
            mImageButton.setTag(R.id.tag_product_entry, beerItem);

            if (beerItem.isFavorite()) {
                mImageButton.setImageResource(R.drawable.ic_favorite_vd_theme_24);
            } else {
                mImageButton.setImageResource(R.drawable.ic_favorite_border_vd_theme_24);
            }

            itemView.setTag(R.id.tag_product_entry, beerItem);
        }
    }


    public BeerAdapter(Context context, List<BeerItem> beerItems, ImageRequester imageRequester) {

        this.imageRequester = imageRequester;
        mContext = context;

        mList = new SortedList<>(BeerItem.class, new SortedList.Callback<BeerItem>() {

            @Override
            public void onInserted(int position, int count) {
                notifyItemRangeInserted(position, count);
            }

            @Override
            public void onRemoved(int position, int count) {
                notifyItemRangeRemoved(position, count);
            }

            @Override
            public void onMoved(int fromPosition, int toPosition) {
                notifyItemMoved(fromPosition, toPosition);
            }

            @Override
            public void onChanged(int position, int count) {
                notifyItemRangeChanged(position, count);
            }

            @Override
            public boolean areContentsTheSame(BeerItem oldItem, BeerItem newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areItemsTheSame(BeerItem item1, BeerItem item2) {
                return item1.getId().equals(item2.getId());
            }

            @Override
            public int compare(BeerItem o1, BeerItem o2) {
                return compareItems(o1, o2);
            }
        });

        int favFilter = CodelabUtil.getIntPreference(CodelabUtil.FAV, mContext);
        for (BeerItem item : beerItems) {

            if (favFilter == 0) {
                mList.add(item);
            } else {
                if (item.isFavorite()) {
                    mList.add(item);
                }
            }

        }

    }

    private int compareItems(BeerItem o1, BeerItem o2) {

        int filter = CodelabUtil.getIntPreference(CodelabUtil.FILTER, mContext);
        int order = CodelabUtil.getIntPreference(CodelabUtil.ORDER, mContext);
        int out = 0;

        switch (filter) {
            case 0:
                out = order == 0 ?
                        (o1.getName()).compareToIgnoreCase(o2.getName()) :
                        (o2.getName()).compareToIgnoreCase(o1.getName());
                break;
            case 1:
                out = order == 0 ?
                        Double.compare(o1.getIbu(), o2.getIbu()) :
                        Double.compare(o2.getIbu(), o1.getIbu());
                break;
            case 2:
                out = order == 0 ?
                        Double.compare(o1.getAbv(), o2.getAbv()) :
                        Double.compare(o2.getAbv(), o1.getAbv());
                break;
            case 3:
                out = order == 0 ?
                        Double.compare(o1.getEbc(), o2.getEbc()) :
                        Double.compare(o2.getEbc(), o1.getEbc());
                break;
        }


        return out;
    }

    @Override
    public BeerAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_beer, viewGroup, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(BeerAdapter.ViewHolder viewHolder, final int position) {

        viewHolder.bind(mList.get(position), imageRequester);

    }

    @Override
    public int getItemCount() {
        return mList.size();
    }

    public void setBeerItems(List<BeerItem> beerItems) {
        mList.clear();
        mList.addAll(beerItems);
        notifyDataSetChanged();
    }

    public void addBeerItem(BeerItem beerItem) {
        mList.add(beerItem);
        notifyDataSetChanged();
    }

    public void addBeerItems(List<BeerItem> beerItems) {

        mList.addAll(beerItems);
        notifyItemRangeInserted(getItemCount() - 1, beerItems.size());
    }

    public void updateBeerItemStatus(String id) {
        for (int i = 0; i < mList.size(); i++) {
            BeerItem beerItem = mList.get(i);
            if (beerItem.getId().equals(id)) {

                beerItem.setFavorite(!beerItem.isFavorite());
                CodelabUtil.saveBeerItemToFile(mContext, beerItem);

                int favFlag = CodelabUtil.getIntPreference(CodelabUtil.FAV, mContext);

                if (favFlag == 0) {
                    notifyItemChanged(i);
                } else {
                    mList.removeItemAt(i);

                }


                break;
            }
        }
    }


    private int getBeerItemPosition(String id) {
        for (int i = 0; i < mList.size(); i++) {
            BeerItem beerItem = mList.get(i);
            if (beerItem.getId().equals(id)) {
                return i;
            }
        }
        return -1;
    }


}
