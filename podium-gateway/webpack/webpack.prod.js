/*
 * Copyright (c) 2017. The Hyve and respective contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 *
 * See the file LICENSE in the root of this repository.
 *
 */

const commonConfig = require('./webpack.common.js');
const webpackMerge = require('webpack-merge');
const CopyWebpackPlugin = require('copy-webpack-plugin');
const ExtractTextPlugin = require("extract-text-webpack-plugin");
const Visualizer = require('webpack-visualizer-plugin');
const ENV = 'prod';
const path = require('path');

module.exports = webpackMerge(commonConfig({env: ENV}), {
        devtool: 'source-map',
        output: {
        path: path.resolve('target/www') ,
        filename: '[hash].[name].bundle.js',
        chunkFilename: '[hash].[id].chunk.js'
    },
    plugins: [
        new ExtractTextPlugin('[hash].styles.css'),
        new Visualizer({
            // Webpack statistics in target folder
            filename: '../stats.html'
        })
    ]
});
