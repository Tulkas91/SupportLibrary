package it.sikuel.reactiveandroid.query;

import android.support.annotation.NonNull;
import android.util.Log;

import it.sikuel.reactiveandroid.ReActiveAndroid;
import it.sikuel.reactiveandroid.internal.notifications.ChangeAction;
import it.sikuel.reactiveandroid.internal.notifications.ModelChangeNotifier;
import it.sikuel.reactiveandroid.internal.utils.QueryUtils;

import io.reactivex.Completable;
import io.reactivex.functions.Action;

public abstract class ExecutableQueryBase<T> extends QueryBase<T> {

    public ExecutableQueryBase(Query parent, Class<T> table) {
        super(parent, table);
    }

    public void execute() {
        QueryUtils.execSQL(table, getSql(), getArgs());
        ModelChangeNotifier.get().notifyTableChanged(table, getChangeAction());
    }

    public Completable executeAsync() {
        return Completable.fromAction(new Action() {
            @Override
            public void run() throws Exception {
                execute();
            }
        });
    }

    @NonNull
    abstract ChangeAction getChangeAction();

}
