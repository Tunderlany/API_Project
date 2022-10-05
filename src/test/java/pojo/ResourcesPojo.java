package pojo;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class ResourcesPojo {

    private int page;
    private int per_page;
    private int total;
    private int total_pages;
    private List<DataPojo> data;
    private SupportPojo support;

}
