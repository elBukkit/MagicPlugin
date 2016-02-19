package com.elmakers.mine.bukkit.data;

import com.elmakers.mine.bukkit.api.data.MageData;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity()
@Table(name="mage")
public class MageDataModel extends MageData {
    public MageDataModel() {
        super(null);
    }

    @Id
    public String getId() {
        return super.getId();
    }

    @Column
    public String getName() {
        return super.getName();
    }

    @Column(columnDefinition = "TEXT")
    public String getData() {
        ConfigurationSection data = this.getExtraData();
        if (data != null || !(data instanceof YamlConfiguration)) {
            return null;
        }
        return ((YamlConfiguration)data).saveToString();
    }

    public void setData(String data) {
        if (data != null && !data.isEmpty()) {
            YamlConfiguration yamlData = new YamlConfiguration();
            try {
                yamlData.loadFromString(data);
                this.setExtraData(yamlData);
            } catch (InvalidConfigurationException ex) {
                ex.printStackTrace();
                this.setExtraData(null);
            }
        } else {
            this.setExtraData(null);
        }
    }
}
