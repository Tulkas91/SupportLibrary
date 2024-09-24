package it.sikuel.reactiveandroid.query;

import android.support.annotation.NonNull;

import it.sikuel.reactiveandroid.internal.notifications.ChangeAction;

abstract class UpdateQueryBase<T> extends ExecutableQueryBase<T> {

    UpdateQueryBase(Query parent, Class<T> table) {
        super(parent, table);
    }

    @NonNull
    @Override
    ChangeAction getChangeAction() {
        return ChangeAction.UPDATE;
    }


}
