
mvn clean test-compile surefire:test -Parq-glassfish-embedded -Dglassfish.database=derby || exit 1
mvn clean test-compile surefire:test -Parq-glassfish-embedded -Dglassfish.database=travis-mysql || exit 1
mvn clean test-compile surefire:test -Parq-glassfish-embedded -Dglassfish.database=travis-postgres || exit 1
mvn clean test-compile surefire:test -Parq-jbossas-remote || exit 1
