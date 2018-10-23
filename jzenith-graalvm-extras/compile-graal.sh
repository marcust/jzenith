#!/bin/sh

set -ue

JAR=../jzenith-example/jzenith-example-redis/target/jzenith-example-redis-0.2-SNAPSHOT-fat.jar

$GRAALVM_HOME/bin/native-image --static \
			       --verbose \
			       --no-server \
			       -Dio.netty.noUnsafe=true \
			       -Dio.netty.noPreferDirect=true \
			       -H:ReflectionConfigurationFiles=reflectconfigs/jzenith.json \
			       -H:+ReportUnsupportedElementsAtRuntime \
			       -H:-UseServiceLoaderFeature \
			       -Dfile.encoding=UTF-8 \
			       --report-unsupported-elements-at-runtime \
			       --delay-class-initialization-to-runtime="io.netty.handler.codec.http.HttpObjectEncoder,io.netty.handler.codec.http2.Http2CodecUtil,io.netty.handler.codec.http2.DefaultHttp2FrameWriter,io.netty.handler.codec.http.websocketx.WebSocket00FrameEncoder" \
			       -jar ${JAR}

