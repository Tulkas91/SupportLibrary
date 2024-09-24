package it.mm.supportlibrary.reactiveandroid.annotation;

/**
 * Represents a SQL COLLATE method for comparing string columns.
 */
public enum ConflictAction {

    ROLLBACK, ABORT, FAIL, IGNORE, REPLACE

}