package com.codeaim.urlcheck.probe.utility;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class Futures
{
    public static <T> CompletableFuture<List<T>> complete(List<CompletableFuture<T>> futures)
    {
        CompletableFuture<Void> allDoneFuture =
                CompletableFuture.allOf(futures.toArray(new CompletableFuture[futures.size()]));
        return allDoneFuture.thenApply(v ->
                futures.stream().
                        map(CompletableFuture::join).
                        collect(Collectors.<T>toList()));
    }
}
