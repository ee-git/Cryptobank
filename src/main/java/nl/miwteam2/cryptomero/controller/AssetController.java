package nl.miwteam2.cryptomero.controller;

import nl.miwteam2.cryptomero.domain.Asset;
import nl.miwteam2.cryptomero.service.AssetService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @author Samuël Geurts & Stijn Klijn
 */

@RestController
@RequestMapping("/assets")
public class AssetController {

    private AssetService assetService;

    @Autowired
    public AssetController(AssetService assetService) {
        this.assetService = assetService;
    }

    @GetMapping
    public List<Asset> getAll() {
        return assetService.getAll();
    }

    @GetMapping("/{name}")
    public Asset findByName(@PathVariable String name) {
        return assetService.findByName(name);
    }
}
