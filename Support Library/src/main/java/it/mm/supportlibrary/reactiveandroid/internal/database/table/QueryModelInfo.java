package it.mm.supportlibrary.reactiveandroid.internal.database.table;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import it.mm.supportlibrary.reactiveandroid.annotation.QueryColumn;
import it.mm.supportlibrary.reactiveandroid.annotation.QueryModel;
import it.mm.supportlibrary.reactiveandroid.internal.serializer.TypeSerializer;
import it.mm.supportlibrary.reactiveandroid.internal.utils.ReflectionUtils;
import it.mm.supportlibrary.reactiveandroid.internal.utils.SQLiteUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains information about table
 */
public final class QueryModelInfo {

    private Class<?> databaseClass;
    private Class<?> modelClass;
    private List<Field> modelFields = new ArrayList<>();
    private Map<Field, ColumnInfo> columns = new LinkedHashMap<>();

    public QueryModelInfo(Class<?> modelClass,
                          Map<Class<?>, TypeSerializer> typeSerializers) {
        QueryModel tableAnnotation = modelClass.getAnnotation(QueryModel.class);

        this.modelClass = modelClass;
        this.databaseClass = tableAnnotation.database();

        List<Field> fields = filterQueryColumnFields(ReflectionUtils.getDeclaredFields(modelClass));
        for (Field field : fields) {
            ColumnInfo columnInfo = createColumnInfo(field, typeSerializers);
            modelFields.add(field);
            columns.put(field, columnInfo);
        }
    }

    @NonNull
    public Class<?> getDatabaseClass() {
        return databaseClass;
    }

    @NonNull
    public Class<?> getModelClass() {
        return modelClass;
    }

    @NonNull
    public List<Field> getFields() {
        return modelFields;
    }

    @NonNull
    public ColumnInfo getColumnInfo(Field field) {
        return columns.get(field);
    }

    private ColumnInfo createColumnInfo(Field field, Map<Class<?>, TypeSerializer> typeSerializers) {
        QueryColumn columnAnnotation = field.getAnnotation(QueryColumn.class);
        String columnName = !TextUtils.isEmpty(columnAnnotation.name()) ? columnAnnotation.name() : field.getName();
        SQLiteType sqliteType = SQLiteUtils.getFieldSQLiteType(field, typeSerializers);
        return new ColumnInfo(columnName, sqliteType, false);
    }

    private List<Field> filterQueryColumnFields(List<Field> modelDeclaredFields) {
        List<Field> modelColumnFields = new ArrayList<>();
        for (Field field : modelDeclaredFields) {
            if (field.isAnnotationPresent(QueryColumn.class)) {
                modelColumnFields.add(field);
            }
        }
        return modelColumnFields;
    }

}
