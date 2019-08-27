package com.mariapps.qdmswiki.articles.view;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.mariapps.qdmswiki.AppConfig;
import com.mariapps.qdmswiki.R;
import com.mariapps.qdmswiki.articles.adapter.ArticlesAdapter;
import com.mariapps.qdmswiki.baseclasses.BaseFragment;
import com.mariapps.qdmswiki.custom.CustomEditText;
import com.mariapps.qdmswiki.custom.CustomProgressBar;
import com.mariapps.qdmswiki.custom.CustomRecyclerView;
import com.mariapps.qdmswiki.documents.adapter.DocumentsAdapter;
import com.mariapps.qdmswiki.home.database.HomeDatabase;
import com.mariapps.qdmswiki.home.model.ArticleModel;
import com.mariapps.qdmswiki.home.model.DocumentModel;
import com.mariapps.qdmswiki.search.view.FolderStructureActivity;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnTextChanged;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class ArticlesFragment extends BaseFragment {

    @BindView(R.id.rvDocuments)
    CustomRecyclerView rvDocuments;
    @BindView(R.id.searchET)
    CustomEditText searchET;
    @BindView(R.id.customProgressBar)
    CustomProgressBar customProgressBar;
    @BindView(R.id.noDataRL)
    RelativeLayout noDataRL;

    private FragmentManager fragmentManager;
    private ArticlesAdapter articlesAdapter;
    private List<ArticleModel> articleList = new ArrayList<>();
    List<String> categoryIds = new ArrayList<>();
    List<String> categoryNames = new ArrayList<>();
    private HomeDatabase homeDatabase;
    private String categoryName;

    @Override
    protected void setUpPresenter() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_articles, container, false);
        ButterKnife.bind(this, view);
        fragmentManager = getFragmentManager();

        rvDocuments.setHasFixedSize(true);
        rvDocuments.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvDocuments.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        homeDatabase = HomeDatabase.getInstance(getActivity());

        getArticlesList();
        return view;
    }

    private void setData() {
        customProgressBar.setVisibility(View.GONE);
        if (articleList != null && articleList.size() > 0) {
            noDataRL.setVisibility(View.GONE);
            GetCategoryName getCategoryName = new GetCategoryName();
            getCategoryName.execute();
        }
        else{
            rvDocuments.setAdapter(null);
            noDataRL.setVisibility(View.VISIBLE);
        }

    }

    public void getArticlesList() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                articleList = homeDatabase.homeDao().getArticles();
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                setData();
            }


            @Override
            public void onError(Throwable e) {

            }
        });
    }



    @OnTextChanged(R.id.searchET)
    void onTextChanged() {
        if (articlesAdapter != null) {
            articlesAdapter.getFilter().filter(searchET.getText().toString());
        }

    }

    public void getCategoryName(List<String> categoryIds,String categoryId,int position) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                categoryName = homeDatabase.homeDao().getCategoryName(categoryId);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                categoryNames.add(position,categoryName);
            }


            @Override
            public void onError(Throwable e) {
            }
        });
    }


    public class GetCategoryName extends AsyncTask<String, Integer, String> {

        ArticleModel articleModel;

        public GetCategoryName() {
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

        }

        @Override
        protected String doInBackground(String... params) {
            try {
                for(int  i=0;i <articleList.size();i++){
                    articleModel = articleList.get(i);
                    categoryIds = new ArrayList<>();
                    categoryIds = articleModel.getCategoryIds();
                    if(categoryIds != null) {
                        for (int j = 0; j < categoryIds.size(); j++) {
                            getCategoryName(categoryIds, categoryIds.get(j), j);
                        }
                        articleModel.setCategoryIds(categoryNames);

                    }
                }
            } catch (Exception e) {
            }

            return "";
        }

        @Override
        protected void onProgressUpdate(Integer... values) {
            super.onProgressUpdate(values);

        }

        @Override
        protected void onPostExecute(String string) {
            setRecyclerView();

        }
    }

    private void setRecyclerView() {
        articlesAdapter = new ArticlesAdapter(getActivity(), articleList);
        rvDocuments.setAdapter(articlesAdapter);
        articlesAdapter.setRowClickListener(new ArticlesAdapter.RowClickListener() {
            @Override
            public void onItemClicked(ArticleModel articleModel) {
                Intent intent = new Intent(getActivity(), FolderStructureActivity.class);
                intent.putExtra(AppConfig.BUNDLE_TYPE, "Article");
                intent.putExtra(AppConfig.BUNDLE_FOLDER_NAME, articleModel.getArticleName());
                intent.putExtra(AppConfig.BUNDLE_FOLDER_ID, articleModel.getId());
                startActivity(intent);

            }
        });
    }

    public void updateArticleList(List<ArticleModel> articleList) {
        this.articleList = articleList;
        setData();
    }
}
