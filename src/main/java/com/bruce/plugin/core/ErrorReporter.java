//package com.bruce.plugin.core;
//
//import com.intellij.diagnostic.AbstractMessage;
//import com.intellij.diagnostic.IdeaReportingEvent;
//import com.intellij.ide.BrowserUtil;
//import com.intellij.openapi.diagnostic.ErrorReportSubmitter;
//import com.intellij.openapi.diagnostic.IdeaLoggingEvent;
//import com.intellij.openapi.diagnostic.Logger;
//import com.intellij.openapi.diagnostic.SubmittedReportInfo;
//import com.intellij.openapi.ide.CopyPasteManager;
//import com.intellij.openapi.progress.ProgressIndicator;
//import com.intellij.openapi.progress.Task;
//import com.intellij.openapi.ui.Messages;
//import com.intellij.openapi.util.SystemInfo;
//import com.intellij.openapi.util.text.StringUtil;
//import com.intellij.util.Consumer;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.awt.*;
//import java.awt.datatransfer.StringSelection;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.util.ArrayList;
//import java.util.Collections;
//import java.util.HashSet;
//import java.util.List;
//import java.util.Set;
//import java.util.concurrent.atomic.AtomicReference;
//
///**
// * @author bruce ge 2023/5/16
// */
//public class ErrorReporter extends ErrorReportSubmitter {
//    private static final Logger LOGGER = Logger.getInstance(ErrorReporter.class);
//
//    @Override
//    public String getReportActionText() {
//        return StringUtil.capitalizeWords(ResourcesLoader.getString("error.submitReport.title"), true);
//    }
//
//    @Override
//    public boolean submit(@NotNull IdeaLoggingEvent[] events, @Nullable String additionalInfo, @NotNull Component parentComponent, @NotNull Consumer<SubmittedReportInfo> consumer) {
//
//        final AtomicReference<String> latestVersionRef = new AtomicReference<>(null);
//        new Task.Modal(null, ResourcesLoader.getString("error.submitReport.checkVersion"), false) {
//            @Override
//            public void run(@NotNull final ProgressIndicator indicator) {
//                try {
//                    latestVersionRef.set(PluginVersionChecker.getLatestVersion(indicator));
//                } catch (final Exception e) {
//                    LOGGER.warn(e);
//                }
//            }
//        }.queue();
//
//        String latestVersion = latestVersionRef.get();
//        if (!StringUtil.isEmptyOrSpaces(latestVersion)) {
//            if (latestVersion.startsWith("v")) {
//                latestVersion = latestVersion.substring(1);
//                final String current = VersionManager.getVersion();
//                if (!latestVersion.equals(current)) {
//                    final int answer = Messages.showYesNoDialog(
//                            ResourcesLoader.getString("error.submitReport.old.text", current, latestVersion),
//                            StringUtil.capitalizeWords(ResourcesLoader.getString("error.submitReport.old.title"), true),
//                            Messages.getQuestionIcon()
//                    );
//                    if (answer == Messages.NO) {
//                        return false;
//                    }
//                }
//            }
//        }
//
//        final SubmittedReportInfo reportInfo = submitImpl(events, additionalInfo, parentComponent);
//        consumer.consume(reportInfo);
//        return true;
//    }
//
//    @NotNull
//    private SubmittedReportInfo submitImpl(
//            @NotNull final IdeaLoggingEvent[] events,
//            @Nullable final String additionalInfo,
//            @NotNull final Component parentComponent
//    ) {
//
//        final StringBuilder body = HelpAction.createProductInfo();
//        if (!StringUtil.isEmptyOrSpaces(additionalInfo)) {
//            body.append("\n\nAdditional Infos\n");
//            body.append(additionalInfo);
//        }
//
//        boolean isFindBugsError = false;
//        final Set<Error> errors = new HashSet<>();
//        for (final IdeaLoggingEvent event : events) {
//            final Error error = createError(event);
//            if (error != null) {
//                errors.add(error);
//                if (!isFindBugsError && error.throwable != null) {
//                    isFindBugsError = FindBugsUtil.isFindBugsError(error.throwable);
//                }
//            }
//        }
//        if (errors.isEmpty()) {
//            throw new IllegalStateException("No error to report");
//        }
//        final List<Error> sorted = new ArrayList<>(errors);
//        Collections.sort(sorted);
//
//        for (final Error error : sorted) {
//            body.append("\n\n").append(error.fullError);
//        }
//
//        final String title = makeTitle(sorted.get(0));
//
//        return openBrowser(
//                isFindBugsError,
//                title,
//                body.toString(),
//                parentComponent
//        );
//    }
//
//    @NotNull
//    private SubmittedReportInfo openBrowser(
//            final boolean isFindBugsError,
//            @NotNull final String title,
//            @NotNull final String errorText,
//            @SuppressWarnings("UnusedParameters") @NotNull final Component parentComponent
//    ) {
//
//        final String baseUrl;
//        if (isFindBugsError) {
//            baseUrl = "https://github.com/spotbugs/spotbugs/issues/";
//        } else {
//            baseUrl = "https://github.com/JetBrains/spotbugs-intellij-plugin/issues";
//        }
//
//        /*
//         * Note: set errorText as body does not work:
//         *   - can cause HTTP 414 Request URI too long
//         *   - if user is not yet logged in github login page will show an error
//         *     502 - "This page is taking way too long to load." (this will also occur with HTTP POST).
//         */
//        final String body = "The error was copied to the clipboard. Press " + (SystemInfo.isMac ? "Command+V" : "Ctrl+V");
//        final String newIssueUrl = baseUrl + "/new?title=" + encode(title) + "&body=" + encode(body);
//
//        CopyPasteManager.getInstance().setContents(new StringSelection(errorText));
//        BrowserUtil.browse(newIssueUrl);
//        return new SubmittedReportInfo(baseUrl, "issue", SubmittedReportInfo.SubmissionStatus.NEW_ISSUE);
//    }
//
//    @NotNull
//    private static String encode(@NotNull final String value) {
//        try {
//            return URLEncoder.encode(value, "UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            throw new InternalError("UTF-8 is not available");
//        }
//    }
//
//    @NotNull
//    private static String makeTitle(@NotNull final Error error) {
//        String ret = error.message;
//        if (ret == null && error.throwable != null) {
//            @SuppressWarnings("ThrowableResultOfMethodCallIgnored")
//            final Throwable cause = ErrorUtil.getCause(error.throwable);
//            ret = cause.toString();
//            if ("java.lang.IllegalStateException".equals(ret)) {
//                ret = "ISE";
//            } else if ("java.lang.NullPointerException".equals(ret)) {
//                ret = "NPE";
//            }
//            final StackTraceElement[] stack = cause.getStackTrace();
//            if (stack.length > 0) {
//                String location = stack[0].toString();
//                if (location.startsWith("org.jetbrains.plugins.spotbugs.")) {
//                    location = location.substring("org.jetbrains.plugins.spotbugs.".length());
//                }
//                ret += " at " + location;
//            }
//        }
//        if (StringUtil.isEmptyOrSpaces(ret)) {
//            ret = "Analysis Error";
//        }
//        return ret;
//    }
//
//    @Nullable
//    private static Error createError(@NotNull final IdeaLoggingEvent event) {
//        String message = event.getMessage();
//        if (StringUtil.isEmptyOrSpaces(message) || "null".equals(message)) {
//            message = null;
//        }
//        final Throwable throwable;
//        // event is a com.intellij.diagnostic.IdeaReportingEvent.TextBasedThrowable
//        // This is a wrapper and is only providing the original stack trace via 'printStackTrace(...)',
//        // but not via 'getStackTrace()'.
//        //
//        // So, we workaround this by retrieving the original exception from the data property
//        if (event instanceof IdeaReportingEvent && event.getData() instanceof AbstractMessage) {
//            throwable = ((AbstractMessage) event.getData()).getThrowable();
//        } else {
//            return null;
//        }
//        final String fullError;
//        if (!StringUtil.equals(message, throwable.getMessage())) {
//            fullError = message + "\n" + StringUtil.getThrowableText(throwable);
//        } else {
//            fullError = StringUtil.getThrowableText(throwable);
//        }
//        return new Error(message, throwable, fullError);
//    }
//
//    private static class Error implements Comparable<Error> {
//        @Nullable
//        private final String message;
//
//        @Nullable
//        private final Throwable throwable;
//
//        @NotNull
//        private final String fullError;
//
//        public Error(
//                @Nullable final String message,
//                @Nullable final Throwable throwable,
//                @NotNull final String fullError
//        ) {
//            this.message = message;
//            this.throwable = throwable;
//            this.fullError = fullError;
//        }
//
//        @Override
//        public int compareTo(@NotNull final Error o) {
//            return fullError.compareTo(o.fullError);
//        }
//
//        @Override
//        public boolean equals(@Nullable final Object o) {
//            if (this == o) return true;
//            if (o == null || getClass() != o.getClass()) return false;
//            Error error = (Error) o;
//            return fullError.equals(error.fullError);
//
//        }
//
//        @Override
//        public int hashCode() {
//            return fullError.hashCode();
//        }
//}
