package sudoku;

import akka.actor.AbstractLoggingActor;
import akka.actor.Props;

class ValidateRowsActor extends AbstractLoggingActor {
    private int validRows = 0;

    @Override
    public Receive createReceive() {
        return receiveBuilder()
                .match(Cell.class, this::validateCell)
                .match(Validate.Valid.class, this::validRow)
                .match(Validate.Invalid.class, this::invalidRow)
                .build();
    }

    @Override
    public void preStart() {
        for (int row = 1; row <= 9; row++) {
            getContext().actorOf(ValidateRowActor.props(row), String.format("validate-row-%d", row));
        }
    }

    private void validateCell(Cell cell) {
        getContext().getChildren().forEach(row -> row.tell(cell, getSelf()));
    }

    @SuppressWarnings("unused")
    private void validRow(Validate.Valid valid) {
        validRows++;
        if (validRows == 9) {
            getContext().getParent().tell(new Validate.Valid("All rows valid"), getSelf());
        }
    }

    private void invalidRow(Validate.Invalid invalid) {
        log().debug("{}", invalid);
    }

    static Props props() {
        return Props.create(ValidateRowsActor.class);
    }
}