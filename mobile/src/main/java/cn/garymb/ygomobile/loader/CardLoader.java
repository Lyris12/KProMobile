package cn.garymb.ygomobile.loader;

import android.app.Dialog;
import android.content.Context;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;

import androidx.annotation.NonNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import cn.garymb.ygomobile.Constants;
import cn.garymb.ygomobile.lite.R;
import cn.garymb.ygomobile.ui.plus.DialogPlus;
import cn.garymb.ygomobile.ui.plus.VUiKit;
import ocgcore.CardManager;
import ocgcore.DataManager;
import ocgcore.LimitManager;
import ocgcore.StringManager;
import ocgcore.data.Card;
import ocgcore.data.LimitList;
import ocgcore.enums.CardType;
import ocgcore.enums.LimitType;

public class CardLoader implements ICardLoader {
    private LimitManager mLimitManager;
    private CardManager mCardManager;
    private StringManager mStringManager;
    private Context context;
    private CallBack mCallBack;
    private LimitList mLimitList;
    private static final String TAG = CardLoader.class.getSimpleName();
    private final static boolean DEBUG = false;

    public interface CallBack {
        void onSearchStart();

        void onLimitListChanged(LimitList limitList);

        void onSearchResult(List<Card> Cards, boolean isHide);

        void onResetSearch();
    }

    public CardLoader(Context context) {
        this.context = context;
        mLimitManager = DataManager.get().getLimitManager();
        mCardManager = DataManager.get().getCardManager();
        mStringManager = DataManager.get().getStringManager();
        mLimitList = mLimitManager.getTopLimit();
    }

    @Override
    public void setLimitList(LimitList limitList) {
        mLimitList = limitList;
        if (mCallBack != null) {
            mCallBack.onLimitListChanged(limitList);
        }
    }

    public SparseArray<Card> readCards(List<Integer> ids, boolean isSorted) {
        return readCards(ids, mLimitList, isSorted);
    }

    public SparseArray<Card> readCards(List<Integer> ids, LimitList limitList, boolean isSorted) {
        if (!isOpen()) {
            return null;
        }
        SparseArray<Card> map = new SparseArray<>();
        if (isSorted) {
            for (Integer id : ids) {
                if (id != 0) {
                    map.put(id, mCardManager.getCard(id));
                }
            }
        } else {
            for (int i = 0; i < ids.size(); i++)
                map.put(i, mCardManager.getCard(ids.get(i)));
        }
        return map;
    }

    public boolean isOpen() {
        return mCardManager.getCount() > 0;
    }

    public void setCallBack(CallBack callBack) {
        mCallBack = callBack;
    }

    public void loadData() {
        loadData(null);
    }

    @Override
    public @NonNull
    LimitList getLimitList() {
        return mLimitList;
    }

    public SparseArray<Card> readAllCardCodes() {
        if (DEBUG) {
            SparseArray<Card> tmp = new SparseArray<>();
            tmp.put(269012, new Card(269012).type(524290L));
            tmp.put(27551, new Card(27551).type(131076L));
            tmp.put(32864, new Card(32864).type(131076L));
            tmp.put(62121, new Card(62121).type(131076L));
            tmp.put(135598, new Card(135598).type(131076L));
            return tmp;
        } else {
            return mCardManager.getAllCards();
        }
    }

    private void loadData(CardSearchInfo searchInfo) {
        if (!isOpen()) {
            return;
        }
        if (Constants.DEBUG)
            Log.i(TAG, "searchInfo=" + searchInfo);
        if (mCallBack != null) {
            mCallBack.onSearchStart();
        }
        Dialog wait = DialogPlus.show(context, null, context.getString(R.string.searching));
        VUiKit.defer().when(() -> {
            List<Card> tmp = new ArrayList<Card>();
            List<Card> monster = new ArrayList<Card>();
            List<Card> spell = new ArrayList<Card>();
            List<Card> trap = new ArrayList<Card>();
            SparseArray<Card> cards = mCardManager.getAllCards();
            int count = cards.size();
            for (int i = 0; i < count; i++) {
                Card card = cards.valueAt(i);
                if (searchInfo == null || searchInfo.check(card)) {
                    if (searchInfo != null && card.Name.equalsIgnoreCase(searchInfo.keyWord1)) {
                        tmp.add(card);
                    } else if (card.isType(CardType.Monster)) {
                        monster.add(card);
                    } else if (card.isType(CardType.Spell)) {
                        spell.add(card);
                    } else if (card.isType(CardType.Trap)) {
                        trap.add(card);
                    }
                }
            }
            Collections.sort(tmp, ASCode);
            Collections.sort(monster, ASC);
            Collections.sort(spell, ASCode);
            Collections.sort(trap, ASCode);
            tmp.addAll(monster);
            tmp.addAll(spell);
            tmp.addAll(trap);
            return tmp;
        }).fail((e) -> {
            if (mCallBack != null) {
                ArrayList<Card> noting = new ArrayList<Card>();
                mCallBack.onSearchResult(noting, false);
            }
            Log.e("kk", "search", e);
            wait.dismiss();
        }).done((tmp) -> {
            if (mCallBack != null) {
                mCallBack.onSearchResult(tmp, false);
            }
            wait.dismiss();
        });
    }

    private Comparator<Card> ASCode = new Comparator<Card>() {
        @Override
        public int compare(Card o1, Card o2) {
            int index1 = (Integer.valueOf(o1.Code).intValue());
            int index2 = (Integer.valueOf(o2.Code).intValue());
            return index1 - index2;
        }
    };

    private Comparator<Card> ASC = new Comparator<Card>() {
        @Override
        public int compare(Card o1, Card o2) {
            if (o1.getStar() == o2.getStar()) {
                if (o1.Attack == o2.Attack) {
                    return (int) (o2.Code - o1.Code);
                } else {
                    return o2.Attack - o1.Attack;
                }
            } else {
                return o2.getStar() - o1.getStar();
            }
        }
    };

    @Override
    public void onReset() {
        if (mCallBack != null) {
            mCallBack.onResetSearch();
        }
    }

    @Override
    public void search(String prefixWord, String suffixWord,
                       long attribute, long level, long race,
                       String limitName, long limit,
                       String atk, String def, long pscale,
                       long setcode, long category, long ot, int linkKey, long... types) {
        CardSearchInfo searchInfo = new CardSearchInfo();
        if (!TextUtils.isEmpty(prefixWord)) {
            searchInfo.keyWord1 = prefixWord;
            searchInfo.keyWordSetcode1 = mStringManager.getSetCode(prefixWord);
        }
        if (!TextUtils.isEmpty(suffixWord)) {
            searchInfo.keyWord2 = suffixWord;
            searchInfo.keyWordSetcode2 = mStringManager.getSetCode(suffixWord);
        }
        searchInfo.attribute = (int) attribute;
        searchInfo.level = (int) level;
        searchInfo.atk = atk;
        searchInfo.def = def;
        searchInfo.ot = (int) ot;
        searchInfo.linkKey = linkKey;
        searchInfo.types = types;

        searchInfo.category = category;
        searchInfo.race = race;
        searchInfo.pscale = (int) pscale;
        searchInfo.setcode = setcode;
        LimitList limitList = null;
        if (!TextUtils.isEmpty(limitName)) {
            limitList = mLimitManager.getLimit(limitName);
            setLimitList(limitList);
            LimitType cardLimitType = LimitType.valueOf(limit);
            if (limitList != null) {
                List<Integer> ids;
                if (cardLimitType == LimitType.Forbidden) {
                    ids = limitList.forbidden;
                } else if (cardLimitType == LimitType.Limit) {
                    ids = limitList.limit;
                } else if (cardLimitType == LimitType.SemiLimit) {
                    ids = limitList.semiLimit;
                } else if (cardLimitType == LimitType.All) {
                    ids = limitList.getCodeList();
                } else {
                    ids = null;
                }
                if (ids != null) {
                    searchInfo.inCards = ids;
                }
            }
        } else {
            setLimitList(null);
        }
        loadData(searchInfo);
    }
}
