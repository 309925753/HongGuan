package com.sk.weichat.db;

import com.j256.ormlite.dao.BaseDaoImpl;
import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.misc.SqlExceptionUtil;
import com.j256.ormlite.support.ConnectionSource;
import com.j256.ormlite.table.DatabaseTable;
import com.j256.ormlite.table.DatabaseTableConfig;

import java.lang.reflect.Constructor;
import java.sql.SQLException;

public class UnlimitDaoManager {

    public synchronized static <D extends Dao<T, ?>, T> D createDao(ConnectionSource connectionSource,
                                                                    DatabaseTableConfig<T> tableConfig) throws SQLException {
        if (connectionSource == null) {
            throw new IllegalArgumentException("connectionSource argument cannot be null");
        }
        return doCreateDao(connectionSource, tableConfig);
    }

    private static Constructor<?> findConstructor(Class<?> daoClass, Object[] params) {
        for (Constructor<?> constructor : daoClass.getConstructors()) {
            Class<?>[] paramsTypes = constructor.getParameterTypes();
            if (paramsTypes.length == params.length) {
                boolean match = true;
                for (int i = 0; i < paramsTypes.length; i++) {
                    if (!paramsTypes[i].isAssignableFrom(params[i].getClass())) {
                        match = false;
                        break;
                    }
                }
                if (match) {
                    return constructor;
                }
            }
        }
        return null;
    }

    private static <D extends Dao<T, ?>, T> D doCreateDao(ConnectionSource connectionSource,
                                                          DatabaseTableConfig<T> tableConfig) throws SQLException {
        Dao<?, ?> dao = null;
        // build the DAO using the table information
        DatabaseTable databaseTable = tableConfig.getDataClass().getAnnotation(DatabaseTable.class);
        if (databaseTable == null || databaseTable.daoClass() == Void.class
                || databaseTable.daoClass() == BaseDaoImpl.class) {
            return null;
        } else {
            Class<?> daoClass = databaseTable.daoClass();
            Object[] arguments = new Object[]{connectionSource, tableConfig};
            Constructor<?> constructor = findConstructor(daoClass, arguments);
            if (constructor == null) {
                throw new SQLException(
                        "Could not find public constructor with ConnectionSource, DatabaseTableConfig parameters in class "
                                + daoClass);
            }
            try {
                dao = (Dao<?, ?>) constructor.newInstance(arguments);
            } catch (Exception e) {
                throw SqlExceptionUtil.create("Could not call the constructor in class " + daoClass, e);
            }
        }

        @SuppressWarnings("unchecked")
        D castDao = (D) dao;
        return castDao;
    }
}
