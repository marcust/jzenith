/**
 * Copyright © 2018 Marcus Thiesen (marcus@thiesen.org)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jzenith.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.reflect.TypeToken;
import io.reactivex.Single;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import org.junit.Test;
import org.jzenith.rest.model.Page;
import org.jzenith.rest.swagger.SingleModelConverter;

import java.util.Iterator;
import java.util.List;
import java.util.Set;

import static java.util.Set.of;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


public class SingleModelConverterTest {

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleModelConverterNoSchemaProperty() {
        final AnnotatedType type = new AnnotatedType();
        type.setSchemaProperty(true);

        final Iterator<ModelConverter> chain = mock(Iterator.class);

        final SingleModelConverter converter = new SingleModelConverter(new ObjectMapper());
        converter.resolve(type, mock(ModelConverterContext.class), chain);

        verify(chain, times(1)).hasNext();

    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleModelConverterNoParamerterizedType() {
        final AnnotatedType type = new AnnotatedType();
        type.setType(Object.class);

        final Iterator<ModelConverter> chain = mock(Iterator.class);

        final SingleModelConverter converter = new SingleModelConverter(new ObjectMapper());
        converter.resolve(type, mock(ModelConverterContext.class), chain);

        verify(chain, times(1)).hasNext();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleModelConverterParamerterizedTypeNotSingle() {
        final AnnotatedType type = new AnnotatedType();
        type.setType(new TypeToken<List<String>>(){}.getType());

        final Iterator<ModelConverter> chain = mock(Iterator.class);

        final SingleModelConverter converter = new SingleModelConverter(new ObjectMapper());
        converter.resolve(type, mock(ModelConverterContext.class), chain);

        verify(chain, times(1)).hasNext();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleModelConverterParamerterizedTypeSingle() {
        final AnnotatedType type = new AnnotatedType();
        type.setType(new TypeToken<Single<String>>(){}.getType());

        final Iterator<ModelConverter> chain = mock(Iterator.class);

        final SingleModelConverter converter = new SingleModelConverter(new ObjectMapper());
        converter.resolve(type, mock(ModelConverterContext.class), chain);

        verify(chain, times(1)).hasNext();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleModelConverterParamerterizedTypeSingleNonSystem() {
        final AnnotatedType type = new AnnotatedType();
        type.setType(new TypeToken<Single<Page<String>>>(){}.getType());

        final Iterator<ModelConverter> chain = mock(Iterator.class);

        final SingleModelConverter converter = new SingleModelConverter(new ObjectMapper());
        converter.resolve(type, mock(ModelConverterContext.class), chain);

        verify(chain, never()).hasNext();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleModelConverterParamerterizedTypeSingleRaw() {
        final AnnotatedType type = new AnnotatedType();
        type.setType(new TypeToken<Single>(){}.getType());

        final Iterator<ModelConverter> chain = mock(Iterator.class);

        final SingleModelConverter converter = new SingleModelConverter(new ObjectMapper());
        converter.resolve(type, mock(ModelConverterContext.class), chain);

        verify(chain, times(1)).hasNext();
    }

    @Test
    @SuppressWarnings("unchecked")
    public void testSingleModelConverterNextChain() {
        final AnnotatedType type = new AnnotatedType();
        type.setSchemaProperty(true);

        final SingleModelConverter converter = new SingleModelConverter(new ObjectMapper());
        converter.resolve(type, mock(ModelConverterContext.class), Set.<ModelConverter>of(converter).iterator());
    }
}

