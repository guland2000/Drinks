package fr.masciulli.drinks.fragment;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;
import fr.masciulli.drinks.R;
import fr.masciulli.drinks.data.DrinksProvider;
import fr.masciulli.drinks.model.DrinkDetailItem;
import fr.masciulli.drinks.util.ConnectionUtils;
import fr.masciulli.drinks.view.ObservableScrollView;
import fr.masciulli.drinks.view.ScrollViewListener;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class DrinkDetailFragment extends Fragment implements ScrollViewListener, Callback<DrinkDetailItem> {

    private ImageView mImageView;
    private TextView mHistoryView;
    private ObservableScrollView mScrollView;
    private TextView mIngredientsView;
    private TextView mInstructionsView;
    private ProgressBar mProgressBar;

    private int mImageViewHeight;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_drink_detail, container, false);

        mImageView = (ImageView)root.findViewById(R.id.image);
        mHistoryView = (TextView)root.findViewById(R.id.history);
        mIngredientsView = (TextView)root.findViewById(R.id.ingredients);
        mInstructionsView = (TextView)root.findViewById(R.id.instructions);
        mScrollView = (ObservableScrollView)root.findViewById(R.id.scroll);
        mProgressBar = (ProgressBar)root.findViewById(R.id.progressbar);

        Intent intent = getActivity().getIntent();
        String name = intent.getStringExtra("drink_name");
        String imageUrl = intent.getStringExtra("drink_imageurl");
        String id = intent.getStringExtra("drink_id");

        if (ConnectionUtils.isOnline(getActivity())) {
            DrinksProvider.getDrink(id, this);
        } else {
            mProgressBar.setVisibility(View.GONE);
            Crouton.makeText(getActivity(), getString(R.string.network_error), Style.ALERT).show();
        }

        getActivity().setTitle(name);
        Picasso.with(getActivity()).load(imageUrl).into(mImageView);

        mImageViewHeight = (int)getResources().getDimension(R.dimen.drink_detail_recipe_margin);
        mScrollView.setScrollViewListener(this);

        return root;
    }

    @Override
    public void onScrollChanged(ObservableScrollView scrollView, int x, int y, int oldx, int oldy) {
        mImageView.setTop((0-y)/2);
        mImageView.setBottom(mImageViewHeight - y);
    }

    @Override
    public void success(DrinkDetailItem drink, Response response) {
        Log.d(this.getClass().getName(), "Drink detail loading has succeeded");

        mProgressBar.setVisibility(View.GONE);
        mScrollView.setVisibility(View.VISIBLE);

        getActivity().setTitle(drink.getName());
        Picasso.with(getActivity()).load(drink.getImageURL()).into(mImageView);
        mHistoryView.setText(drink.getHistory());


        String htmlString = "";
        int i = 0;
        for (String ingredient : drink.getIngredients()) {
            if (++i == drink.getIngredients().size()) {
                htmlString += "&#8226; " + ingredient;
            } else {
                htmlString += "&#8226; " + ingredient + "<br>";
            }
        }
        mIngredientsView.setText(Html.fromHtml(htmlString));

        mInstructionsView.setText(drink.getInstructions());
    }

    @Override
    public void failure(RetrofitError error) {
        Crouton.makeText(getActivity(), R.string.detail_loading_failed, Style.ALERT).show();
        mProgressBar.setVisibility(View.GONE);
        
        Response resp = error.getResponse();
        String message;
        if(resp != null) {
            message = "response status : " + resp.getStatus();
        } else {
            message = "no response";
        }
        Log.e(this.getClass().getName(), "Drink detail loading has failed : " + message);
    }
}
