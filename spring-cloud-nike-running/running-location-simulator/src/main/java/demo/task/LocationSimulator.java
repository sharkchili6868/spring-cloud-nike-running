package demo.task;

import demo.model.*;
import demo.service.PositionService;
import demo.support.NavUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class LocationSimulator implements Runnable {

    @Getter
    @Setter
    private long id;

    @Setter
    private PositionService positionInfoService;

    // flag for cancelling the simulation
    private AtomicBoolean cancel = new AtomicBoolean();

    private double speedInMps;

    // determine whether the runner should continue to move or not.
    // Say, if the runner has already got to his destination, shouldMove = false.
    private boolean shouldMove;
    private boolean exportPositionsToMessaging = true;
    private Integer reportInterval = 500; // in ms

    @Getter
    @Setter
    private PositionInfo currentPosition = null;

    @Setter
    private List<Leg> legs;
    private RunnerStatus runnerStatus = RunnerStatus.NONE;
    private String runningId;

    @Setter
    private Point startPoint;
    private Date executionStartTime;

    private MedicalInfo medicalInfo;

    public LocationSimulator(GpsSimulatorRequest gpsSimulatorRequest) {
        this.shouldMove = gpsSimulatorRequest.isMove();
        this.exportPositionsToMessaging =
                gpsSimulatorRequest.isExportPositionsToMessaging();
        this.setSpeed(gpsSimulatorRequest.getSpeed());
        this.reportInterval = gpsSimulatorRequest.getReportInterval();
        this.runningId = gpsSimulatorRequest.getRunningId();
        this.runnerStatus = gpsSimulatorRequest.getRunnerStatus();
        this.medicalInfo = gpsSimulatorRequest.getMedicalInfo();
    }

    public void setSpeed(double speed) {
        this.speedInMps = speed;
    }

    @Override
    public void run() {
        try {
            executionStartTime = new Date();
            if (cancel.get()) {
                destroy();
                return;
            }

            while (!Thread.interrupted()) {
                long startTime = new Date().getTime();

                if (currentPosition != null) {
                    if (shouldMove) {
                        moveRunningLocation();
                        currentPosition.setSpeed(speedInMps);
                    } else {
                        // arrived destination
                        currentPosition.setSpeed(0.0);
                    }

                    currentPosition.setRunnerStatus(this.runnerStatus);

                    final MedicalInfo medicalInfoToUse;

                    switch (this.runnerStatus) {
                        case SUPPLY_NOW:
                        case SUPPLY_SOON:
                        case STOP_NOW:
                            medicalInfoToUse = this.medicalInfo;
                            break;
                        default:
                            medicalInfoToUse = null;
                            break;
                    }

                    // construct current position that will be sent to distribution service
                    final CurrentPosition currentPosition = new
                            CurrentPosition(this.currentPosition.getRunningId(),
                            new
                                    Point(this.currentPosition.getPosition().getLatitude(),
                                    this.currentPosition.getPosition().getLongitude()),
                            this.currentPosition.getRunnerStatus(),
                            this.currentPosition.getSpeed(),
                            this.currentPosition.getLeg().getHeading(),
                            medicalInfoToUse
                    );

                    // send current position to distribution service via REST API
                    // @TODO implement positionInfoService
                    positionInfoService.processPositionInfo(id, currentPosition,
                            this.exportPositionsToMessaging);
                }

                sleep(startTime);
            }
        } catch (InterruptedException ie) {
            destroy();
            return;
        }

        destroy();
    }

    public void destroy() {
        currentPosition = null;
    }

    public double getSpeed() {
        return this.speedInMps;
    }

    public synchronized void cancel() {
        this.cancel.set(true);
    }

    private void sleep(long startTime) throws InterruptedException {
        long endTime = new Date().getTime();
        long elapsedTime = endTime - startTime;
        long sleepTime = reportInterval - elapsedTime > 0 ? reportInterval - elapsedTime : 0;
        Thread.sleep(sleepTime);
    }

    private void moveRunningLocation() {
        double distance = speedInMps * reportInterval / 1000.0;
        double distanceFromStart = currentPosition.getDistanceFromStart() + distance;
        double excess = 0.0;

        for (int i = currentPosition.getLeg().getId(); i < legs.size(); i++) {
            Leg currentLeg = legs.get(i);
            excess = distanceFromStart > currentLeg.getLength() ? distanceFromStart - currentLeg.getLength() : 0.0;

            if (Double.doubleToLongBits(excess) == 0) {
                // this means new position falls within current leg
                currentPosition.setDistanceFromStart(distanceFromStart);
                currentPosition.setLeg(currentLeg);
                //@TODO implement the new position calculation method in NavUtils

                Point newPosition =
                        NavUtils.getPosition(currentLeg.getStartPosition(), distanceFromStart,
                                currentLeg.getHeading());;
                currentPosition.setPosition(newPosition);
                return;
            }
            distanceFromStart = excess;
        }
        setStartPosition();
    }

    // Position running location at start of path
    private void setStartPosition() {
        currentPosition = new PositionInfo();
        currentPosition.setRunningId(this.runningId);
        Leg leg = legs.get(0);
        currentPosition.setLeg(leg);
        currentPosition.setPosition(leg.getStartPosition());
        currentPosition.setDistanceFromStart(0.0);
    }
}
