package com.renova.imagetools.strategy;

import com.renova.imagetools.util.FileNameFilter;
import com.renova.imagetools.util.LatestModifiedFileFilter;
import com.renova.imagetools.util.RuntimeData;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.FileFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class ModifiedFileLookUpStrategyWithModifiedDate extends DefaultModifiedFileLookUpStrategy {

    @Override
    protected IOFileFilter createFileFilter(Optional<String> fileExtension) {
        Date lastLookUpDate = RuntimeData.getInstance().getLastLookUpDate();
        List<IOFileFilter> filters = new ArrayList<>();
        filters.add(FileFileFilter.FILE);
        filters.add(new LatestModifiedFileFilter(lastLookUpDate));
        filters.add(new FileNameFilter("[0-9]+\\..+"));
        fileExtension.ifPresent(extension -> filters.add(new FileNameFilter(".*\\." + extension)));
        return new AndFileFilter(filters);
    }
}
