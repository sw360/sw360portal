/*
 * Copyright (c) Bosch Software Innovations GmbH 2015-2016.
 * Part of the SW360 Portal Project.
 *
 * SPDX-License-Identifier: EPL-1.0
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.eclipse.sw360.cvesearch.entitytranslation;

import java.util.function.Function;

public interface EntityTranslator<T, R> extends Function<T, R> {

}
