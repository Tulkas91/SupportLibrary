package it.sikuel.reactiveandroid.query;

import android.support.annotation.NonNull;

import it.sikuel.reactiveandroid.internal.notifications.ChangeAction;
import it.sikuel.reactiveandroid.internal.utils.QueryUtils;

abstract class InsertQueryBase<T> extends ExecutableQueryBase<T> {

    InsertQueryBase(Query parent, Class<T> table) {
        super(parent, table);
    }

    @NonNull
    @Override
    ChangeAction getChangeAction() {
        return ChangeAction.INSERT;
    }


}
