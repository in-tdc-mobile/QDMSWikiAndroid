package com.mariapps.qdmswiki.search.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatImageView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.Toast;

import com.mariapps.qdmswiki.AppConfig;
import com.mariapps.qdmswiki.R;
import com.mariapps.qdmswiki.baseclasses.BaseFragment;
import com.mariapps.qdmswiki.bookmarks.view.BookmarkActivity;
import com.mariapps.qdmswiki.custom.CustomEditText;
import com.mariapps.qdmswiki.custom.CustomTextView;
import com.mariapps.qdmswiki.documents.view.DocumentInfoViewActivity;
import com.mariapps.qdmswiki.home.database.HomeDao;
import com.mariapps.qdmswiki.home.database.HomeDatabase;
import com.mariapps.qdmswiki.home.model.ArticleModel;
import com.mariapps.qdmswiki.home.model.DocumentModel;
import com.mariapps.qdmswiki.home.model.RecentlyViewedModel;
import com.mariapps.qdmswiki.home.presenter.HomePresenter;
import com.mariapps.qdmswiki.utils.DateUtils;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.Completable;
import io.reactivex.CompletableObserver;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Action;
import io.reactivex.schedulers.Schedulers;

public class DocumentViewFragment extends BaseFragment {

    @BindView(R.id.searchET)
    CustomEditText searchET;
    @BindView(R.id.showMenuFab)
    FloatingActionButton showMenuFab;
    @BindView(R.id.webView)
    WebView webView;

    private String folderName;
    private String folderId;
    private String name;
    private String type;
    private DocumentModel documentModel;
    private String id;
    private String documentData;
    private String version;
    private ArticleModel articleModel;
    private HomeDatabase homeDatabase;
    private RecentlyViewedModel recentlyViewedModel;
    private String articleData;

    @Override
    protected void setUpPresenter() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_document_view, container, false);
        ButterKnife.bind(this, view);

        try {
            Bundle args = getArguments();
            id = args.getString(AppConfig.BUNDLE_ID, "");
            name = args.getString(AppConfig.BUNDLE_NAME, "");
            type = args.getString(AppConfig.BUNDLE_TYPE, "");
            folderId = args.getString(AppConfig.BUNDLE_FOLDER_ID, "");
            folderName = args.getString(AppConfig.BUNDLE_FOLDER_NAME, "");
            version = args.getString(AppConfig.BUNDLE_VERSION, "");

            if(type.equals("Document")) {
                recentlyViewedModel = new RecentlyViewedModel(id, name, folderId, folderName, version, DateUtils.getCurrentDate());
                insertRecentlyViewedDocument(recentlyViewedModel);
            }

        } catch (Exception e) {
        }

        homeDatabase = HomeDatabase.getInstance(getActivity());
        loadDocument();

        return view;
    }


    @OnClick({R.id.showMenuFab})

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.showMenuFab:
                View popupView = View.inflate(getActivity(), R.layout.menu_pop_up, null);
                PopupWindow popupWindow = new PopupWindow(popupView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT, true);
                popupWindow.showAtLocation(popupView, Gravity.BOTTOM, 0, 50);
                popupWindow.setOutsideTouchable(false);

                View container = (View) popupWindow.getContentView().getParent();
                WindowManager wm = (WindowManager) getActivity().getSystemService(Context.WINDOW_SERVICE);
                WindowManager.LayoutParams p = (WindowManager.LayoutParams) container.getLayoutParams();
                p.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND;
                p.dimAmount = 0.5f;
                wm.updateViewLayout(container, p);

                FrameLayout.LayoutParams lp = (FrameLayout.LayoutParams) popupView.getLayoutParams();

                // Set TextView layout margin 25 pixels to all side
                // Left Top Right Bottom Margin
                lp.setMargins(50, 25, 50, 0);

                // Apply the updated layout parameters to TextView
                popupView.setLayoutParams(lp);

                LinearLayout linInfo = popupView.findViewById(R.id.linInfo);
                LinearLayout linBookmark = popupView.findViewById(R.id.linBookmark);
                LinearLayout linDownload = popupView.findViewById(R.id.linDownload);

                linInfo.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), DocumentInfoViewActivity.class);
                        intent.putExtra(AppConfig.BUNDLE_FOLDER_NAME, folderName);
                        intent.putExtra(AppConfig.BUNDLE_ID, id);
                        intent.putExtra(AppConfig.BUNDLE_FOLDER_ID,folderId);
                        startActivity(intent);
                    }
                });

                linBookmark.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), BookmarkActivity.class);
                        intent.putExtra(AppConfig.BUNDLE_FOLDER_NAME, folderName);
                        intent.putExtra(AppConfig.BUNDLE_ID, id);
                        startActivity(intent);
                    }
                });

                break;
        }
    }

    public void loadDocument() {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {

                if(type.equals("Document")){
                documentModel = homeDatabase.homeDao().getDocumentData(id);
                documentData = documentModel.getDocumentData();
                }
                else{
                    documentModel = new DocumentModel();
                    articleModel = homeDatabase.homeDao().getArticleData(id);
                    documentData = articleModel.getDocumentData();
                }
                documentData = documentData.replace("<script src=\"/WikiPALApp/Scripts/TemplateSettings/toc-template-settings.js\"></script>", "<script src=\"./Scripts/toc-template-settings.js.download\"></script>");
                documentData = documentData.replace("\n", "");
                documentModel.setDocumentData(documentData);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                setHTMLContent();
            }


            @Override
            public void onError(Throwable e) {
                Toast.makeText(getContext(),e.getMessage(),Toast.LENGTH_LONG).show();
            }
        });
    }

    private void setHTMLContent() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.getSettings().setDisplayZoomControls(false);
        webView.getSettings().setBuiltInZoomControls(true);
        webView.addJavascriptInterface(new AppJavaScriptProxy(getActivity()), "androidAppProxy");

        webView.setWebViewClient(new WebViewClient() {

//            public void onLoadResource(WebView view, String url) {
//                // Check to see if there is a progress dialog
//                if (progressDialog == null) {
//                    // If no progress dialog, make one and set message
//                    progressDialog = new ProgressDialog(activity);
//                    progressDialog.setMessage("Loading please wait...");
//                    progressDialog.show();
//
//                    // Hide the webview while loading
//                    webView.setEnabled(false);
//                }
//            }

            public void onPageFinished(WebView view, String url) {
                // Page is done loading;
                // hide the progress dialog and show the webview

                webView.setEnabled(true);
            }

            @Override
            public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
                Toast.makeText(getActivity(), "Your Internet Connection May not be active Or " + error.getDescription(), Toast.LENGTH_LONG).show();
            }
        });

        webView.loadUrl("file:///android_asset/templateHTML/TemplateHTML.html");
//        webView.loadDataWithBaseURL("", data, "text/html", "UTF-8", "");
    }

    public void loadArticle(String id) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                articleModel = ((ArticleModel) homeDatabase.homeDao().getArticleData(id));
                String articleData = articleModel.getDocumentData();
                articleData = articleData.replace("<script src=\"/WikiPALApp/Scripts/TemplateSettings/toc-template-settings.js\"></script>", "<script src=\"./Scripts/toc-template-settings.js.download\"></script>");
                articleData = articleData.replace("\n", "");
                articleModel.setDocumentData(articleData);
            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {
                webView.post(new Runnable() {
                    @Override
                    public void run() {
                        webView.loadUrl("javascript:setArticleDataFromViewController('" + articleModel.getDocumentData() + "','" + articleModel.getId() + "')");
                    }
                });
            }


            @Override
            public void onError(Throwable e) {

            }
        });
    }

    public void insertRecentlyViewedDocument(RecentlyViewedModel recentlyViewedModel) {
        Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                homeDatabase.homeDao().insertRecentlyViewedDocument(recentlyViewedModel);

            }
        }).observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io()).subscribe(new CompletableObserver() {
            @Override
            public void onSubscribe(Disposable d) {

            }

            @Override
            public void onComplete() {

            }


            @Override
            public void onError(Throwable e) {

            }
        });
    }

    public class AppJavaScriptProxy {

        private Activity activity = null;

        public AppJavaScriptProxy(Activity activity) {
            this.activity = activity;
        }

        @JavascriptInterface
        public String getDocumentData() {
            return documentData;

        }

        @JavascriptInterface
        public void getArtcileData(String id) {
            loadArticle(id);
        }

        @JavascriptInterface
        public void showToast(String message) {
            Toast.makeText(getActivity(), "ALERT " + message, Toast.LENGTH_LONG).show();
        }


    }

    class Article {
        private String data = null;
        private String name = null;

        public Article(String data, String name) {
            this.data = data;
            this.name = name;
        }
    }
}
