package br.com.demandwise.controller;

import br.com.demandwise.dto.DashboardResponse;
import br.com.demandwise.service.DashboardService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/metricas")
    public DashboardResponse metricas() {
        return dashboardService.obterMetricas();
    }
}
