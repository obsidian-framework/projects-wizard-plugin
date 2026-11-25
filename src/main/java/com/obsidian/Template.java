package com.obsidian;

public enum Template {
    DEFAULT("Default App (Base Obsidian)", "obsidian"),
    FLINT("Flint (Starter Kit avec Auth)", "flint"),
    LIVECOMPONENTS("LiveComponents Examples", "livecomponents-examples"),
    REALTIME("Realtime Examples (SSE / Live feeds)", "realtime-examples");

    private static final String GITHUB_BASE = "https://github." + "com/obsidian-framework/";

    private final String label;
    private final String repo;

    Template(String label, String repo) {
        this.label = label;
        this.repo = repo;
    }

    public String zipUrl() {
        return GITHUB_BASE + repo + "/archive/HEAD.zip";
    }

    /** Used by the ComboBox renderer. */
    @Override
    public String toString() {
        return label;
    }
}
