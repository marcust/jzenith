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
package org.jzenith.rest.swagger;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import io.reactivex.Single;
import io.swagger.v3.core.converter.AnnotatedType;
import io.swagger.v3.core.converter.ModelConverter;
import io.swagger.v3.core.converter.ModelConverterContext;
import io.swagger.v3.core.jackson.ModelResolver;
import io.swagger.v3.core.util.ReflectionUtils;
import io.swagger.v3.oas.models.media.Schema;
import lombok.NonNull;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Iterator;

public class SingleModelConverter extends ModelResolver {

    public SingleModelConverter(ObjectMapper mapper) {
        super(mapper);
    }

    @Override
    public Schema resolve(@NonNull final AnnotatedType annotatedType, @NonNull final ModelConverterContext context, @NonNull final Iterator<ModelConverter> chain) {
        if (annotatedType.isSchemaProperty()) {
            return continueChain(annotatedType, context, chain);
        }

        final Type actualType = annotatedType.getType();
        if (!(actualType instanceof ParameterizedType)) {
            return continueChain(annotatedType, context, chain);
        }

        final ParameterizedType parameterizedType = (ParameterizedType) actualType;
        if (parameterizedType.getRawType() != Single.class) {
            return continueChain(annotatedType, context, chain);
        }

        final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();

        Preconditions.checkState(actualTypeArguments.length > 0, "Single should always have a type argument");

        final Type actualTypeArgument = actualTypeArguments[0];

        final AnnotatedType newAnnotatedType = new AnnotatedType(actualTypeArgument);
        final JavaType actualJavaType = _mapper.constructType(actualTypeArgument);
        if (ReflectionUtils.isSystemType(actualJavaType)) {
            return continueChain(annotatedType, context, chain);
        }
        newAnnotatedType.setSkipSchemaName(false);
        return super.resolve(newAnnotatedType, context, chain);
    }

    private Schema continueChain(final @NonNull AnnotatedType type, final @NonNull ModelConverterContext context, final @NonNull Iterator<ModelConverter> chain) {
        if (chain.hasNext()) {
            return chain.next().resolve(type, context, chain);
        } else {
            return null;
        }
    }

}
