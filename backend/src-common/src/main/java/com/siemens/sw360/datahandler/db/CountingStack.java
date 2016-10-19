/*
 * Copyright Siemens AG, 2016. Part of the SW360 Portal Project.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.siemens.sw360.datahandler.db;

import java.util.HashMap;
import java.util.Stack;

/**
 * A Stack that counts how many times each item has been pushed during the lifetime of the stack object.
 * (i.e. counts are not decremented when an item is popped.)
 *
 * @author: alex.borodin@evosoft.com
 */
class CountingStack<E> extends Stack<E> {
    private HashMap<E, Integer> counts = new HashMap<>();

    @Override
    public E push(E item) {
        int count = getCount(item);
        counts.put(item, count + 1);
        return super.push(item);
    }

    public int getCount(E item) {
        return counts.containsKey(item) ? counts.get(item) : 0;
    }
}
