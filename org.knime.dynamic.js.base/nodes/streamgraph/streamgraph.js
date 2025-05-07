/* global d3:false, kt:false, nv:false, moment:false */
window.knimeStreamgraph = (function () {

    var view = {};

    var stackStyleByType = {
        'Stacked-Area-Chart': 'stack',
        'Percentage-Area-Chart': 'expand',
        'Stream-Graph': 'stream-center'
    };
    var MIN_HEIGHT = 100;
    var MIN_WIDTH = 100;
    var currentFilter = null;
    var TOOLTIP_WARNING = 'basisTooltip';

    var _representation, _value, _data, _colorRange, layoutContainer,

        chart, svg, knimeTable1, knimeTable2, xAxisType, xAxisData,

        /**
         * Function declarations
         */
        drawControls, setColors, setXAxisConf, transformData, drawChart, toggleFilter, setCssClasses,
        createXAxisFormatter, updateTitles, updateAxisLabels, toggleGrid, toggleLegend, setTooltipCssClasses,
        filterChanged;

    view.init = function (representation, value) {
        _representation = representation;
        _value = value;
        // Create Knime tables from data.
        // Load data from port 1.
        knimeTable1 = new kt();
        knimeTable1.setDataTable(_representation.inObjects[0]);
        // var columnKeys = _representation.options.columns;

        // Load data from port 2.
        knimeTable2 = null;
        if (_representation.inObjects[1] !== null) {
            knimeTable2 = new kt();
            knimeTable2.setDataTable(_representation.inObjects[1]);
        }

        // Set locale for moment.js.
        if (_representation.options.dateTimeFormats.globalDateTimeLocale !== 'en') {
            moment.locale(_representation.options.dateTimeFormats.globalDateTimeLocale);
        }

        if (_representation.options.enableViewControls) {
            drawControls();
        }
        setColors();
        setXAxisConf();
        transformData();
        drawChart();
        toggleFilter();
    };

    drawChart = function () {
        // Remove earlier chart.
        d3.select('#layoutContainer').remove();

        /*
         * Parse some options.
         */
        var stackStyle = stackStyleByType[_value.options.chartType];
        var optFullscreen = _representation.options.svg.fullscreen && _representation.runningInView;
        var isTitle = _value.options.title !== '' || _value.options.subtitle !== '';

        /*
         * Create HTML for the view.
         */
        var body = d3.select('body');

        // Determine available witdh and height.
        var width, height;
        if (optFullscreen) {
            width = '100%';

            if (isTitle || !_representation.options.enableViewControls) {
                knimeService.floatingHeader(true);
                height = '100%';
            } else {
                knimeService.floatingHeader(false);
                height = 'calc(100% - ' + knimeService.headerHeight() + 'px)';
            }

        } else {
            width = _representation.options.svg.width + 'px';
            height = _representation.options.svg.height + 'px';
        }

        layoutContainer = body.append('div').attr('id', 'layoutContainer').attr('class', 'knime-layout-container')
            .style({
                width: width,
                height: height,
                'min-width': MIN_WIDTH + 'px',
                'min-height': MIN_HEIGHT + 'px',
                position: 'absolute'
            });

        // create div container to hold svg
        var svgContainer = layoutContainer.append('div').attr('id', 'svgContainer')
            .attr('class', 'knime-svg-container').style({
                'min-width': MIN_WIDTH + 'px',
                'min-height': MIN_HEIGHT + 'px',
                width: '100%',
                height: '100%'
            });

        // Create the SVG object
        svg = svgContainer.append('svg').attr('id', 'svg');

        if (optFullscreen) {
            svg.attr('width', '100%');
            svg.attr('height', '100%');
        } else {
            svg.attr('width', width);
            svg.attr('height', height);
        }

        if (_value.options.interpolation === 'basis' && _value.options.interactiveGuideline) {
            knimeService.setWarningMessage(
                'Displaying a tooltip is not supported when interpolation is set to "basis".', TOOLTIP_WARNING);
        } else {
            knimeService.clearWarningMessage(TOOLTIP_WARNING);
        }

        // create the stacked area chart
        nv.addGraph(function () {
            chart = nv.models.stackedAreaChart().margin({
                right: 50
            }).x(function (d) {
                return d[0];
            }).y(function (d) {
                return d[1];
            }).color(_colorRange).interpolate(_value.options.interpolation).style(stackStyle).showControls(false)
                .showLegend(true).useInteractiveGuideline(
                    _value.options.interpolation === 'basis' ? false : _value.options.interactiveGuideline)
                .interactive(false).duration(0);

            chart.dispatch.on('renderEnd.css', setCssClasses);

            var topMargin = 10;
            topMargin += _value.options.title ? 10 : 0;
            topMargin += _value.options.legend ? 0 : 30;
            topMargin += _value.options.subtitle ? 8 : 0;
            var bottomMargin = _value.options.title || _value.options.subtitle ? 25 : 30;
            chart.legend.margin({
                top: topMargin,
                bottom: topMargin
            });
            chart.margin({
                top: topMargin,
                bottom: bottomMargin
            });

            chart.xAxis.tickFormat(createXAxisFormatter());

            chart.yAxis.tickFormat(d3.format(_representation.options.yAxisFormatString));

            updateTitles(false);
            updateAxisLabels(false);

            svg.datum(_data).call(chart);

            nv.utils.windowResize(chart.update);

            if ('disabled' in _value.options) {
                var state = chart.defaultState();
                state.disabled = _value.options.disabled;
                chart.dispatch.changeState(state);
            }

            toggleGrid();
            toggleLegend();

            // tooltip is re-created every time therefore we need to assign classes accordingly
            chart.interactiveLayer.dispatch.on('elementMousemove.tooltipCss', setTooltipCssClasses);

            return chart;
        });
    };

    toggleGrid = function () {
        var opacity = _value.options.showGrid ? 1 : 0;
        d3.selectAll('g.tick:not(.zero) > line').style('opacity', opacity);
    };

    toggleLegend = function () {
        var opacity = _value.options.legend ? 1 : 0;
        d3.select('g.nv-legend').style('opacity', opacity);
    };

    setXAxisConf = function () {
        // Set data and data type for the x-axis.
        var xAxisColumn = _representation.options.xAxisColumn;
        if (typeof xAxisColumn === 'undefined') {
            // If undefined: The user selected RowId as x-Axis.
            xAxisType = 'string';
            xAxisData = [];

            var rows = knimeTable1.getRows();
            for (var i = 0; i < rows.length; i++) {
                xAxisData.push(rows[i].rowKey);
            }

        } else {
            var columnIndex = knimeTable1.getColumnNames().indexOf(xAxisColumn);
            xAxisType = knimeTable1.getColumnTypes()[columnIndex];
            if (xAxisType === 'dateTime') {
                // need to get which exactly date&time type it is
                xAxisType = knimeTable1.getKnimeColumnTypes()[columnIndex];
            }
            xAxisData = knimeTable1.getColumn(columnIndex);
        }
    };

    // Transform the tabular format into a JSON format.
    transformData = function () {
        // Check which rows are included by the filter.
        var includedRows = [];
        for (var j = 0; j < knimeTable1.getNumRows(); j++) {
            if (!currentFilter || knimeTable1.isRowIncludedInFilter(j, currentFilter)) {

                includedRows.push(j);
            }
        }

        _data = [];
        var columns = _representation.options.columns;
        // Loop over all columns.
        for (var i = 0; i < columns.length; i++) {
            var columnKey = columns[i];
            var columnIndex = knimeTable1.getColumnNames().indexOf(columnKey);
            var currentColumn = knimeTable1.getColumn(columnIndex);

            _data.push({
                key: columnKey,
                values: includedRows.map(
                // This loops over all rows that are included.
                function (i) {
                    var d = currentColumn[i];

                    if (xAxisType === 'number') {
                        // If data type of x-axis column can be interpreted as numeric,
                        // use the data for the x-axis.
                        return [xAxisData[i], d];
                    } else {
                        // If not, just use an integer index [0, n[.
                        return [i, d];
                    }
                })
            });
        }
    };

    toggleFilter = function () {
        if (_value.options.subscribeFilter) {
            knimeService.subscribeToFilter(_representation.tableIds[0], filterChanged, knimeTable1.getFilterIds());
        } else {
            knimeService.unsubscribeFilter(_representation.tableIds[0], filterChanged);
        }
    };

    filterChanged = function (filter) {
        currentFilter = filter;
        transformData();
        svg.datum(_data);
        chart.update();
    };

    // Set color scale: custom or default.
    setColors = function () {
        var colorScale = [];
        var columns = _representation.options.columns;
        if (knimeTable2 === null) {
            colorScale = d3.scale.category10();
            if (columns.length > 10) {
                colorScale = d3.scale.category20();
            }
            _colorRange = colorScale.range();
        } else {
            var rowColors = knimeTable2.getRowColors();
            var numColumns = columns.length;
            for (var i = 0; i < numColumns; i++) {
                var columnName = columns[i];
                var rowIndex = knimeTable2.getColumn(0).indexOf(columnName);
                var color = rowColors[rowIndex];

                if (!color) {
                    color = '#7C7C7C';
                }
                colorScale.push(color);
            }
            _colorRange = colorScale;
        }
    };
    
    // Return a function to format the x-axis-ticks.
    createXAxisFormatter = function () {
        /* eslint indent: [2, 4, {"SwitchCase": 1}]*/
        switch (xAxisType) {
            case 'Date and Time':
                return function (i) {
                    return moment(xAxisData[i]).utc().format(
                        _representation.options.dateTimeFormats.globalDateTimeFormat);
                };
            case 'Date':
                return function (i) {
                    return moment(xAxisData[i]).format(_representation.options.dateTimeFormats.globalLocalDateFormat);
                };
            case 'Date&time (Local)':
                return function (i) {
                    return moment(xAxisData[i]).format(
                        _representation.options.dateTimeFormats.globalLocalDateTimeFormat);
                };
            case 'Time':
                return function (i) {
                    return moment(xAxisData[i], 'hh:mm:ss.SSSSSSSSS').format(
                        _representation.options.dateTimeFormats.globalLocalTimeFormat);
                };
            case 'Date&time (Zoned)':
                return function (i) {
                    var data = xAxisData[i];
                    var regex = /(.*)\[(.*)\]$/;
                    var match = regex.exec(data);
                    var date, dateTimeOffset;
                    
                    if (match === null) {
                        date = moment.tz(data, '');
                    } else {
                        dateTimeOffset = match[1];
                        date = moment.tz(dateTimeOffset, _representation.options.dateTimeFormats.timezone);
                    }

                    return date.format(_representation.options.dateTimeFormats.globalZonedDateTimeFormat);
                };
            case 'string':
                return function (i) {
                    return xAxisData[i];
                };
            case 'number':
                return d3.format(_representation.options.xAxisFormatString);
            default:
                return function (i) {
                    return i;
                };
        }
    };

    updateTitles = function (updateChart) {
        if (chart) {
            var curTitle = d3.select('#title');
            var curSubtitle = d3.select('#subtitle');
            var chartNeedsUpdating = curTitle.empty() === Boolean(_value.options.title) ||
                curSubtitle.empty() === Boolean(_value.options.subtitle);
            if (_value.options.title) {
                if (curTitle.empty()) {
                    svg.append('text').attr('x', 20).attr('y', 30).attr('id', 'title').attr('class', 'knime-title')
                        .text(_value.options.title);
                } else {
                    curTitle.text(_value.options.title);
                }
            } else {
                curTitle.remove();
            }
            
            if (_value.options.subtitle) {
                if (curSubtitle.empty()) {
                    svg.append('text').attr('x', 20).attr('y', _value.options.title ? 46 : 20).attr('id', 'subtitle')
                        .attr('class', 'knime-subtitle').text(_value.options.subtitle);
                } else {
                    curSubtitle.text(_value.options.subtitle).attr('y', _value.options.title ? 46 : 20);
                }
            } else {
                curSubtitle.remove();
            }

            if (updateChart && chartNeedsUpdating) {
                var topMargin = 10;
                topMargin += _value.options.title ? 10 : 0;
                topMargin += _value.options.legend ? 0 : 30;
                topMargin += _value.options.subtitle ? 8 : 0;
                var bottomMargin = 25;
                bottomMargin += (_value.options.title || _value.options.subtitle) ? 0 : 5;
                bottomMargin += _value.options.xAxisLabel ? 20 : 0;
                chart.legend.margin({
                    top: topMargin,
                    bottom: topMargin
                });
                chart.margin({
                    top: topMargin,
                    bottom: bottomMargin
                });

                if (_representation.options.svg.fullscreen && _representation.runningInView) {

                    var isTitle = _value.options.title !== '' || _value.options.subtitle !== '';
                    var height;
                    
                    if (isTitle || !_representation.options.enableViewControls) {
                        knimeService.floatingHeader(true);
                        height = '100%';
                    } else {
                        knimeService.floatingHeader(false);
                        height = 'calc(100% - ' + knimeService.headerHeight() + 'px)';
                    }

                    layoutContainer.style('height', height)
                    // two rows below force to invalidate the container which solves a weird problem with vertical
                    // scroll bar in IE
                    .style('display', 'none').style('display', 'block');
                    // d3.select("#svgContainer").style("height", height);
                }

                chart.update();
            }
        }
    };

    updateAxisLabels = function (updateChart) {
        if (chart) {
            var curYAxisLabel = '';
            var curXAxisLabel = '';
            var curYAxisLabelElement = d3.select('.nv-y.nv-axis .nv-axislabel');
            var curXAxisLabelElement = d3.select('.nv-x.nv-axis .nv-axislabel');
            if (!curYAxisLabelElement.empty()) {
                curYAxisLabel = curYAxisLabelElement.text();
            }
            if (!curXAxisLabelElement.empty()) {
                curXAxisLabel = curXAxisLabelElement.text();
            }
            var chartNeedsUpdating = (curYAxisLabel !== _value.options.yAxisLabel) ||
                (curXAxisLabel !== _value.options.xAxisLabel);

            if (!chartNeedsUpdating) {
                return;
            }

            chart.xAxis.axisLabel(_value.options.xAxisLabel).axisLabelDistance(0);

            chart.yAxis.axisLabel(_value.options.yAxisLabel).axisLabelDistance(0);

            var bottomMargin = 25;
            bottomMargin += _value.options.title || _value.options.subtitle ? 0 : 5;
            bottomMargin += _value.options.xAxisLabel ? 20 : 0;

            var leftMargin = 60;
            leftMargin += _value.options.yAxisLabel ? 15 : 0;

            chart.margin({
                left: leftMargin,
                bottom: bottomMargin
            });

            if (updateChart) {
                chart.update();
            }
        }
    };
    
    // eslint-disable-next-line complexity
    drawControls = function () {
        if (!knimeService) {
            return;
        }

        if (_representation.options.displayFullscreenButton) {
            knimeService.allowFullscreen();
        }

        if (!_representation.options.enableViewControls) {
            return;
        }

        // Title / Subtitle Configuration
        var titleEdit = _representation.options.enableTitleEdit;
        var subtitleEdit = _representation.options.enableSubtitleEdit;
        if (titleEdit) {
            var chartTitleText = knimeService.createMenuTextField('chartTitleText', _value.options.title, function () {
                if (_value.options.title !== this.value) {
                    _value.options.title = this.value;
                    updateTitles(true);
                }
            }, true);
            knimeService.addMenuItem('Chart Title:', 'header', chartTitleText);
        }
        if (subtitleEdit) {
            var chartSubtitleText = knimeService.createMenuTextField('chartSubtitleText', _value.options.subtitle,
                function () {
                    if (_value.options.subtitle !== this.value) {
                        _value.options.subtitle = this.value;
                        updateTitles(true);
                    }
                }, true);
            knimeService.addMenuItem('Chart Subtitle:', 'header', chartSubtitleText, null, knimeService.SMALL_ICON);
        }

        // x-Axis & y-Axis Labels
        var xAxisEdit = _representation.options.enableXAxisEdit;
        var yAxisEdit = _representation.options.enableYAxisEdit;
        if (xAxisEdit || yAxisEdit) {
            knimeService.addMenuDivider();

            if (xAxisEdit) {
                var xAxisText = knimeService.createMenuTextField('xAxisText', _value.options.xAxisLabel, function () {
                    if (_value.options.xAxisLabel !== this.value) {
                        _value.options.xAxisLabel = this.value;
                        updateAxisLabels(true);
                    }
                }, true);
                knimeService.addMenuItem('X-axis label:', 'ellipsis-h', xAxisText);
            }
            if (yAxisEdit) {
                var yAxisText = knimeService.createMenuTextField('yAxisText', _value.options.yAxisLabel, function () {
                    if (_value.options.yAxisLabel !== this.value) {
                        _value.options.yAxisLabel = this.value;
                        updateAxisLabels(true);
                    }
                }, true);
                knimeService.addMenuItem('Y-axis label:', 'ellipsis-v', yAxisText);
            }
        }

        // Chart Type / Interpolation Method / Custom Color
        var chartTypeChange = _representation.options.enableChartTypeChange;
        var interpolationEdit = _representation.options.enableInterpolationMethodEdit;
        if (chartTypeChange || interpolationEdit) {
            knimeService.addMenuDivider();

            if (chartTypeChange) {
                var chartTypes = Object.keys(stackStyleByType);
                var chartTypeSelector = knimeService.createMenuSelect('chartTypeSelector', _value.options.chartType,
                    chartTypes, function () {
                        _value.options.chartType = this.options[this.selectedIndex].value;
                        drawChart(); // needs a redraw to avoid tooltip problem (AP-7068)
                    });
                knimeService.addMenuItem('Chart Type:', 'area-chart', chartTypeSelector);
            }

            if (interpolationEdit) {
                var interpolationMethods = ['basis', 'linear', 'step'];
                var interpolationMethodSelector = knimeService.createMenuSelect('interpolationMethodSelector',
                    _value.options.interpolation, interpolationMethods, function () {
                        var changedToBasis = this.options[this.selectedIndex].value === 'basis' &&
                            _value.options.interpolation !== 'basis';
                        _value.options.interpolation = this.options[this.selectedIndex].value;
                        if (changedToBasis && _value.options.interactiveGuideline) {
                            drawChart();
                        } else {
                            knimeService.clearWarningMessage(TOOLTIP_WARNING);
                            chart.interpolate(_value.options.interpolation);
                            chart.useInteractiveGuideline(_value.options.interpolation === 'basis' ? false
                                : _value.options.interactiveGuideline);
                            chart.update();
                        }
                    });
                // CHECK: Should we use line-chart here?
                knimeService.addMenuItem('Interpolation:', 'bar-chart', interpolationMethodSelector);
            }
        }

        // Legend, Interactive Guideline, Grid
        var legendToggle = _representation.options.enableLegendToggle;
        var interactiveGuidelineToggle = _representation.options.enableInteractiveGuidelineToggle;
        var showGridToggle = _representation.options.showGridToggle;
        if (legendToggle || interactiveGuidelineToggle || showGridToggle) {
            knimeService.addMenuDivider();

            if (legendToggle) {
                var legendCheckbox = knimeService.createMenuCheckbox('legendCheckbox', _value.options.legend,
                    function () {
                        _value.options.legend = this.checked;
                        toggleLegend();
                    });
                knimeService.addMenuItem('Legend:', 'info-circle', legendCheckbox);
            }

            if (interactiveGuidelineToggle) {
                var interactiveGuidelineCheckbox = knimeService.createMenuCheckbox('interactiveGuidelineCheckbox',
                    _value.options.interactiveGuideline, function () {
                        _value.options.interactiveGuideline = this.checked;
                        drawChart();
                    });

                knimeService.addMenuItem('Tooltip:', 'comment', interactiveGuidelineCheckbox);
            }

            if (showGridToggle) {
                var gridCheckbox = knimeService.createMenuCheckbox('gridCheckbox', _value.options.showGrid,
                    function () {
                        _value.options.showGrid = this.checked;
                        toggleGrid();
                    });
                knimeService.addMenuItem('Show Grid:', 'th', gridCheckbox);
            }
        }

        // Filter event checkbox.
        if (knimeService.isInteractivityAvailable()) {
            knimeService.addMenuDivider();
            var subFilIcon = knimeService.createStackedIcon('filter', 'angle-double-right', 'faded right sm',
                'left bold');

            var subFilCheckbox = knimeService.createMenuCheckbox('filterCheckbox', _value.options.subscribeFilter,
                function () {
                    _value.options.subscribeFilter = this.checked;
                    toggleFilter();
                });
            knimeService.addMenuItem('Subscribe to filter', subFilIcon, subFilCheckbox);
        }
    };

    setCssClasses = function () {
        // axis
        var axis = d3.selectAll('.nv-axis').classed('knime-axis', true);
        d3.selectAll('.nv-x').classed('knime-x', true);
        d3.selectAll('.nv-y').classed('knime-y', true);
        d3.selectAll('.nv-axislabel').classed('knime-axis-label', true);
        axis.selectAll('path.domain').classed('knime-axis-line', true);
        var axisMaxMin = d3.selectAll('.nv-axisMaxMin').classed('knime-axis-max-min', true);
        axisMaxMin.selectAll('text').classed('knime-tick-label', true);
        var tick = axis.selectAll('.knime-axis .tick').classed('knime-tick', true);
        tick.selectAll('text').classed('knime-tick-label', true);
        tick.selectAll('line').classed('knime-tick-line', true);

        // legend
        d3.selectAll('.nv-legendWrap').classed('knime-legend', true);
        d3.selectAll('.nv-legend-symbol').classed('knime-legend-symbol', true);
        d3.selectAll('.nv-legend-text').classed('knime-legend-label', true);
    };

    setTooltipCssClasses = function () {
        // tooltip
        var tooltip = d3.selectAll('.nvtooltip').classed('knime-tooltip', true);
        tooltip.selectAll('.x-value').classed('knime-tooltip-caption', true).classed('knime-x', true);
        tooltip.selectAll('.legend-color-guide').classed('knime-tooltip-color', true);
        tooltip.selectAll('.key').classed('knime-tooltip-key', true);
        tooltip.selectAll('.value').classed('knime-tooltip-value', true);
    };

    view.validate = function () {
        return true;
    };

    view.getComponentValue = function () {
        // Save disabled-state of the series from the chart if:
        // - it was saved in _value before
        // - some series are disabled

        var container = d3.select('#svgContainer');
        var disabled = container.selectAll('g .nv-series').data().map(function (o) {
            return Boolean(o.disabled);
        });

        if ('disabled' in _value.options || disabled.some(Boolean)) {
            _value.options.disabled = disabled;
        }

        return _value;
    };

    view.getSVG = function () {
        // correct faulty rect elements
        d3.selectAll('rect').each(function () {
            var rect = d3.select(this);
            if (!rect.attr('width')) {
                rect.attr('width', 0);
            }
            if (!rect.attr('height')) {
                rect.attr('height', 0);
            }
        });
        var svgElement = d3.select('svg')[0][0];
        knimeService.inlineSvgStyles(svgElement);
        // Return the SVG as a string.
        return (new XMLSerializer()).serializeToString(svgElement);
    };

    return view;

})();
