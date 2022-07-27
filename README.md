jOOQ Reactive Transactional Delete Fails
===
This project reproduces a bug in jOOQ which causes delete operations in reactive transactions to fail silently. This uses testcontainers to run postgresql in a docker container, so requires that the docker be installed to run properly.

The application creates a table, inserts 100 rows, deletes all rows in a transaction, then deletes all rows without using a transaction. The output should be:
```
Created USERS table
There are currently 0 users
Inserted 100 users
There are currently 100 users
Deleted 100 users in transaction
There are currently 0 users
Deleted 0 users without a transaction
There are currently 0 users
```
Instead, the first delete operation does not seem to effect the table at all, resulting the following output:
```
Created USERS table
There are currently 0 users
Inserted 100 users
There are currently 100 users
Deleted 100 users in transaction
There are currently 100 users
Deleted 100 users without a transaction
There are currently 0 users
```
In addition to the output described above, a lot of warnings and errors are logged to stderr by the reactor project, such as the following:
```
19:46:12.035 [reactor-tcp-epoll-1] [ERROR] r.n.c.ChannelOperationsHandler- [33600423, L:/127.0.0.1:48384 ! R:localhost/127.0.0.1:49753] Error was received while reading the incoming data. The connection will be closed.
java.lang.IllegalStateException: Already resumed, but proposed with update CompletedExceptionally[org.jooq.exception.DataAccessException: SQL [select count(*) from USERS]; Connection closed]
	at kotlinx.coroutines.CancellableContinuationImpl.alreadyResumedError(CancellableContinuationImpl.kt:482)
	at kotlinx.coroutines.CancellableContinuationImpl.resumeImpl(CancellableContinuationImpl.kt:447)
	at kotlinx.coroutines.CancellableContinuationImpl.resumeImpl$default(CancellableContinuationImpl.kt:420)
	at kotlinx.coroutines.CancellableContinuationImpl.resumeWith(CancellableContinuationImpl.kt:328)
	at kotlinx.coroutines.reactive.AwaitKt$awaitOne$2$1.onError(Await.kt:277)
	at org.jooq.impl.Internal$1.onError(Internal.java:459)
	at org.jooq.impl.R2DBC$AbstractResultSubscriber.onError(R2DBC.java:278)
	at reactor.core.publisher.StrictSubscriber.onError(StrictSubscriber.java:106)
	at io.r2dbc.postgresql.util.FluxDiscardOnCancel$FluxDiscardOnCancelSubscriber.onError(FluxDiscardOnCancel.java:97)
	at reactor.core.publisher.FluxMapFuseable$MapFuseableSubscriber.onError(FluxMapFuseable.java:140)
	at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.onError(FluxContextWrite.java:121)
	at reactor.core.publisher.FluxWindowPredicate$WindowPredicateMain.signalAsyncError(FluxWindowPredicate.java:352)
	at reactor.core.publisher.FluxWindowPredicate$WindowPredicateMain.checkTerminated(FluxWindowPredicate.java:534)
	at reactor.core.publisher.FluxWindowPredicate$WindowPredicateMain.drainLoop(FluxWindowPredicate.java:486)
	at reactor.core.publisher.FluxWindowPredicate$WindowPredicateMain.drain(FluxWindowPredicate.java:430)
	at reactor.core.publisher.FluxWindowPredicate$WindowPredicateMain.onError(FluxWindowPredicate.java:289)
	at io.r2dbc.postgresql.util.FluxDiscardOnCancel$FluxDiscardOnCancelSubscriber.onError(FluxDiscardOnCancel.java:97)
	at reactor.core.publisher.FluxContextWrite$ContextWriteSubscriber.onError(FluxContextWrite.java:121)
	at reactor.core.publisher.FluxCreate$BaseSink.error(FluxCreate.java:453)
	at reactor.core.publisher.FluxCreate$BufferAsyncSink.drain(FluxCreate.java:781)
	at reactor.core.publisher.FluxCreate$BufferAsyncSink.error(FluxCreate.java:726)
	at reactor.core.publisher.FluxCreate$SerializedFluxSink.drainLoop(FluxCreate.java:230)
	at reactor.core.publisher.FluxCreate$SerializedFluxSink.next(FluxCreate.java:169)
	at io.r2dbc.postgresql.client.ReactorNettyClient$Conversation.emit(ReactorNettyClient.java:635)
	at io.r2dbc.postgresql.client.ReactorNettyClient$BackendMessageSubscriber.emit(ReactorNettyClient.java:887)
	at io.r2dbc.postgresql.client.ReactorNettyClient$BackendMessageSubscriber.onNext(ReactorNettyClient.java:761)
	at io.r2dbc.postgresql.client.ReactorNettyClient$BackendMessageSubscriber.onNext(ReactorNettyClient.java:667)
	at reactor.core.publisher.FluxHandle$HandleSubscriber.onNext(FluxHandle.java:119)
	at reactor.core.publisher.FluxPeekFuseable$PeekConditionalSubscriber.onNext(FluxPeekFuseable.java:854)
	at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onNext(FluxMap.java:220)
	at reactor.core.publisher.FluxMap$MapConditionalSubscriber.onNext(FluxMap.java:220)
	at reactor.netty.channel.FluxReceive.drainReceiver(FluxReceive.java:279)
	at reactor.netty.channel.FluxReceive.onInboundNext(FluxReceive.java:388)
	at reactor.netty.channel.ChannelOperations.onInboundNext(ChannelOperations.java:404)
	at reactor.netty.channel.ChannelOperationsHandler.channelRead(ChannelOperationsHandler.java:93)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357)
	at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:327)
	at io.netty.handler.codec.ByteToMessageDecoder.fireChannelRead(ByteToMessageDecoder.java:314)
	at io.netty.handler.codec.ByteToMessageDecoder.callDecode(ByteToMessageDecoder.java:435)
	at io.netty.handler.codec.ByteToMessageDecoder.channelRead(ByteToMessageDecoder.java:279)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
	at io.netty.channel.AbstractChannelHandlerContext.fireChannelRead(AbstractChannelHandlerContext.java:357)
	at io.netty.channel.DefaultChannelPipeline$HeadContext.channelRead(DefaultChannelPipeline.java:1410)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:379)
	at io.netty.channel.AbstractChannelHandlerContext.invokeChannelRead(AbstractChannelHandlerContext.java:365)
	at io.netty.channel.DefaultChannelPipeline.fireChannelRead(DefaultChannelPipeline.java:919)
	at io.netty.channel.epoll.AbstractEpollStreamChannel$EpollStreamUnsafe.epollInReady(AbstractEpollStreamChannel.java:800)
	at io.netty.channel.epoll.EpollEventLoop.processReady(EpollEventLoop.java:480)
	at io.netty.channel.epoll.EpollEventLoop.run(EpollEventLoop.java:378)
	at io.netty.util.concurrent.SingleThreadEventExecutor$4.run(SingleThreadEventExecutor.java:986)
	at io.netty.util.internal.ThreadExecutorMap$2.run(ThreadExecutorMap.java:74)
	at io.netty.util.concurrent.FastThreadLocalRunnable.run(FastThreadLocalRunnable.java:30)
	at java.base/java.lang.Thread.run(Thread.java:833)
```
