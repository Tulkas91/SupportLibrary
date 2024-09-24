package it.mm.supportlibrary.reactiveandroid.query;

import android.support.annotation.NonNull;

import it.mm.supportlibrary.reactiveandroid.internal.notifications.ChangeAction;
import it.mm.supportlibrary.reactiveandroid.internal.notifications.ModelChangeNotifier;
import it.mm.supportlibrary.reactiveandroid.internal.utils.QueryUtils;

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
