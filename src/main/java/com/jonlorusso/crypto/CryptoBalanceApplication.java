package com.jonlorusso.crypto;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.BitmapEncoder.BitmapFormat;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYChartBuilder;
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle;
import org.knowm.xchart.style.Styler.LegendPosition;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import com.binance.api.client.BinanceApiClientFactory;
import com.binance.api.client.BinanceApiRestClient;
import com.jonlorusso.crypto.entity.Ticker;
import com.jonlorusso.crypto.entity.TickerGroup;
import com.jonlorusso.crypto.entity.TradeGroup;
import com.jonlorusso.crypto.repository.TickerGroupRepository;
import com.jonlorusso.crypto.repository.TickerRepository;
import com.jonlorusso.crypto.repository.TradeGroupRepository;
import com.jonlorusso.crypto.service.TradeService;

@Controller
@EnableScheduling
@SpringBootApplication
public class CryptoBalanceApplication extends WebMvcConfigurerAdapter implements CommandLineRunner {

	@Autowired
	private TickerRepository tickerRepository;

	@Autowired
	private TradeGroupRepository tradeGroupRepository;

	@Autowired
	private TickerGroupRepository tickerGroupRepository;

	@Autowired
	private TradeService tradeService;

	@Bean
	public BinanceApiRestClient binanceApiRestClient() {
		// BinanceApiClientFactory binanceApiClientFactory = BinanceApiClientFactory.newInstance("API-KEY", "SECRET");
		BinanceApiClientFactory binanceApiClientFactory = BinanceApiClientFactory.newInstance();
		return binanceApiClientFactory.newRestClient();
	}
	
	@RequestMapping(value = "/graph/trades", produces = "application/json")
	@ResponseBody
	public Map<String, Object> tradesData() {
		List<TradeGroup> tradeGroups = tradeGroupRepository.findTop30ByOrderByTimestampDesc();
		Collections.reverse(tradeGroups);
		
		Map<String, Object> data = new HashMap<>();
		data.put("data", tradeGroups.stream().map(t -> t.getBTCValue()).collect(Collectors.toList()));
		data.put("labels", tradeGroups.stream().map(t -> t.getTimestamp()).collect(Collectors.toList()));
		return data;
	}
	
	@RequestMapping(value = "/graph/tickers", produces = "application/json")
	@ResponseBody
	public Map<String, Object> tickersData() {
		List<TickerGroup> tickerGroups = tickerGroupRepository.findTop100ByOrderByTimestampDesc();
		Collections.reverse(tickerGroups);
		Map<String, Object> data = new HashMap<>();
		data.put("data", tickerGroups.stream().map(tg -> tg.getTickers().stream().filter(t -> t.getDayPercentChange() > 0.0).count()).collect(Collectors.toList()));
		data.put("labels", tickerGroups.stream().map(t -> t.getTimestamp()).collect(Collectors.toList()));
		return data;
	}
	
	@RequestMapping("/trades")
	public String trades(Model model) {
		model.addAttribute("tradeGroups", tradeGroupRepository.findTop30ByOrderByTimestampDesc());
		return "trades";
	}

	@RequestMapping("/tickers")
	public String tickers(Model model) {
		model.addAttribute("tickerGroup", tickerGroupRepository.findTopByOrderByTimestampDesc());
		return "tickers";
	}

	@RequestMapping("/balances")
	public String holdings(Model model) {
		model.addAttribute("balances", tradeGroupRepository.findTopByOrderByTimestampDesc().getBalances());
		return "balances";
	}

	@RequestMapping("/")
	public String index(Model model) {
		return "index";
	}

	private Map<Long, List<Ticker>> getTickersByTimestamp() {
		List<Ticker> allTickers = tickerRepository.findByTimestampBetween(0, 1000000000000000l);

		HashMap<Long, List<Ticker>> tickersByTimestamp = new HashMap<>();
		allTickers.forEach(ticker -> {
			long timestamp = ticker.getTimestamp();
			List<Ticker> tickers = tickersByTimestamp.get(timestamp);
			if (tickers == null) {
				tickers = new ArrayList<>();
			}
			tickers.add(ticker);
			tickersByTimestamp.put(timestamp, tickers);
		});

		return tickersByTimestamp;
	}

	public XYChart getChart(String title, Method tickerMethod, int gainPercent) {
		Map<Long, List<Ticker>> tickersByTimestamp = getTickersByTimestamp();

		Set<Long> timestamps = new TreeSet<>(tickersByTimestamp.keySet());
		double[] xData = timestamps.stream().mapToDouble(l -> (double) l).toArray();
		double[] yData = timestamps.stream()
				.map(ts -> tickersByTimestamp.get(ts).stream()
						.filter(t -> (Double) ReflectionUtils.invokeMethod(tickerMethod, t) > gainPercent).count())
				.mapToDouble(l -> (double) l).toArray();

		XYChart chart = QuickChart.getChart(title, "time", "count", "f", xData, yData);
		chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);

		return chart;
	}

	@GetMapping("/graph/holdings")
	public ResponseEntity<byte[]> graphHoldings() throws IOException {
		String title = "btc value last 10 trades";

	    final XYChart chart = new XYChartBuilder().width(600).height(400).title(title).xAxisTitle("time").yAxisTitle("BTC").build();

	    chart.getStyler().setLegendPosition(LegendPosition.InsideNE);
	    chart.getStyler().setDefaultSeriesRenderStyle(XYSeriesRenderStyle.Line);
	    
	    List<TradeGroup> tradeGroups = tradeGroupRepository.findTop30ByOrderByTimestampDesc();
	    double[] xData = IntStream.range(0, tradeGroups.size()).mapToDouble(t -> t).toArray();

	    double[] yData = tradeGroups.stream().sorted().mapToDouble(t -> t.getBTCValue()).toArray();
	    chart.addSeries("trades", xData, yData);

	    double max = Arrays.stream(yData).max().getAsDouble();
	    double min = Arrays.stream(yData).min().getAsDouble();
	    double multiplier = (max - min) / 20;
	    
	    System.out.println("min: " + min);
	    System.out.println("max: " + max);
	    System.out.println("multiplier: " + multiplier);
	    
	    yData = tradeGroups.stream().sorted().mapToDouble(tg -> (tg.getTickerGroup().getTickers().stream().filter(t -> t.getDayPercentChange() > 0.0).count() * multiplier) + min).toArray();
//	    yData = tradeGroups.stream().mapToDouble(tg -> 0.995).toArray();
	    chart.addSeries("positiveCurrencies", xData, yData);

		System.out.println(Arrays.toString(xData));
		System.out.println(Arrays.toString(yData));

		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=chart.png")
				.contentType(MediaType.IMAGE_PNG).body(BitmapEncoder.getBitmapBytes(chart, BitmapFormat.PNG));
	}

	@GetMapping("/graph/24h")
	public ResponseEntity<byte[]> graph24h(@RequestParam(defaultValue = "0") int gain) throws IOException {
		String title = String.format("Number of coins (in Top 20) with positive 24h percent change greater than %d%%",
				gain);
		Method tickerMethod = ReflectionUtils.findMethod(Ticker.class, "getDayPercentChange");
		XYChart chart = getChart(title, tickerMethod, gain);
		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=chart.png")
				.contentType(MediaType.IMAGE_PNG).body(BitmapEncoder.getBitmapBytes(chart, BitmapFormat.PNG));
	}

	@GetMapping("/graph/1h")
	public ResponseEntity<byte[]> graph1h(@RequestParam(defaultValue = "0") int gain) throws IOException {
		String title = String.format("Number of coins (in Top 20) with positive 1h percent change greater than %d%%",
				gain);
		Method tickerMethod = ReflectionUtils.findMethod(Ticker.class, "getHourPercentChange");
		XYChart chart = getChart(title, tickerMethod, gain);
		return ResponseEntity.ok().header("Content-Disposition", "attachment; filename=chart.png")
				.contentType(MediaType.IMAGE_PNG).body(BitmapEncoder.getBitmapBytes(chart, BitmapFormat.PNG));
	}

	public static void main(String[] args) {
		SpringApplication.run(CryptoBalanceApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		tradeService.initializeBalance();
	}

	@Override
	public void addResourceHandlers(ResourceHandlerRegistry registry) {
		registry.addResourceHandler("/webjars/**").addResourceLocations("classpath:/META-INF/resources/webjars/");
	}
}
