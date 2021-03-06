package ru.r2cloud.sdr;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ru.r2cloud.model.SdrStatus;
import ru.r2cloud.util.Configuration;
import ru.r2cloud.util.ProcessFactory;
import ru.r2cloud.util.ProcessWrapper;
import ru.r2cloud.util.Util;

class PlutoStatusProcess implements SdrStatusProcess {

	private static final Logger LOG = LoggerFactory.getLogger(PlutoStatusProcess.class);

	private ProcessWrapper process;
	private boolean terminated = false;
	private final Configuration config;
	private final ProcessFactory factory;

	PlutoStatusProcess(Configuration config, ProcessFactory factory) {
		this.config = config;
		this.factory = factory;
	}

	@Override
	public SdrStatus getStatus() {
		SdrStatus result = null;
		try {
			BufferedReader r = null;
			synchronized (this) {
				if (terminated) {
					terminated = false;
					return result;
				}
				process = factory.create(config.getProperty("satellites.plutosdr.test.path") + " -a", false, false);
				r = new BufferedReader(new InputStreamReader(process.getInputStream()));
				terminated = false;
			}
			String curLine = null;
			while ((curLine = r.readLine()) != null && !Thread.currentThread().isInterrupted()) {
				if (curLine.startsWith("No IIO context")) {
					result = new SdrStatus();
					result.setDongleConnected(false);
					result.setError(curLine);
					break;
				}
			}
			if (result == null) {
				result = new SdrStatus();
				result.setDongleConnected(true);
			}
		} catch (IOException e) {
			String error = "unable to read status";
			result = new SdrStatus();
			result.setDongleConnected(false);
			result.setError(error);
			LOG.error(error, e);
		} finally {
			stop(5000);
		}
		return result;
	}

	@Override
	public synchronized void terminate(long timeout) {
		shutdown(timeout);
		terminated = true;
	}

	synchronized void stop(long timeout) {
		shutdown(timeout);
		terminated = false;
	}

	private void shutdown(long timeout) {
		if (process == null) {
			return;
		}
		Util.shutdown("pluto-status", process, timeout);
		process = null;
	}

}
