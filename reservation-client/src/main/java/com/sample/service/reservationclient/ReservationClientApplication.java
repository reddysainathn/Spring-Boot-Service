package com.sample.service.reservationclient;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.circuitbreaker.EnableCircuitBreaker;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.netflix.feign.EnableFeignClients;
import org.springframework.cloud.netflix.feign.FeignClient;
import org.springframework.cloud.netflix.zuul.EnableZuulProxy;
import org.springframework.cloud.stream.annotation.Output;
import org.springframework.context.annotation.Bean;
import org.springframework.hateoas.Resources;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

@SpringBootApplication
@EnableEurekaClient
@EnableDiscoveryClient
@EnableZuulProxy
@EnableFeignClients
@EnableCircuitBreaker
public class ReservationClientApplication {

	/*
	--Replaced By Feign --Load Balancing can be done by Feign @EnableFeignClients
	@Bean
	@LoadBalanced
	RestTemplate restTemplate(){
		return new RestTemplate();
	}

	*/

	public static void main(String[] args) {
		SpringApplication.run(ReservationClientApplication.class, args);
	}
}
@FeignClient("reservation-service")
interface ReservationReader {
	@RequestMapping(method = RequestMethod.GET , value = "/reservations")
	Resources<Reservation> read();
}
class Reservation {

	public String name;

	public String getReservationName() {
		return name;
	}
}
@RestController
@RequestMapping("/reservations")
class ReservationAPIGateway {

	/*
	--Replaced By Feign --Load Balancing can be done by Feign @EnableFeignClients
	private final RestTemplate restTemplate;

	@Autowired
	public ReservationAPIGateway(@LoadBalanced RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}
	*/

	private final ReservationReader reservationReader;

	@Autowired
	public ReservationAPIGateway(ReservationReader reservationReader) {
		this.reservationReader = reservationReader;
	}


	public Collection<String> fallback(){
		return new ArrayList<>();
	}

	@HystrixCommand(fallbackMethod = "fallback")
	@RequestMapping(method = RequestMethod.GET,value = "/names")
	public Collection<String> names(){
		return this.reservationReader
				.read()
				.getContent()
				.stream()
				.map(Reservation::getReservationName)
				.collect(Collectors.toList());
	}


interface ReservationChannels {


}

}