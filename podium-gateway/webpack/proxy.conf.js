function setupProxy() {
    const tls = process.env.TLS;
    const remoteTestServerOptions = {
        target: "https://podium-test.thehyve.net",
        secure: true,
        changeOrigin: true,
    };
    const localTestServerOptions = {
        target: `http${tls ? "s" : ""}://localhost:8080`,
        secure: false,
    };
    let useOptions = localTestServerOptions;
    const conf = [
        {
            context: [
                "/podiumuaa",
                "/api",
                "/management",
                "/swagger-resources",
                "/v2/api-docs",
                "/v3/api-docs",
                "/h2-console",
            ],
            ...useOptions,
        },
    ];
    return conf;
}

module.exports = setupProxy();
