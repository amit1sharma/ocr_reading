    implementation ('org.springframework.boot:spring-boot-starter-web')
	implementation 'org.springframework.boot:spring-boot-starter-log4j2'
    testImplementation('org.springframework.boot:spring-boot-starter-test') {
        exclude group: 'org.junit.vintage', module: 'junit-vintage-engine'
    }
	implementation 'org.springframework.boot:spring-boot-starter-web-services' 
	providedRuntime('org.springframework.boot:spring-boot-starter-tomcat')
	implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
	implementation 'wsdl4j:wsdl4j:1.6.3' 
	testImplementation 'org.springframework.boot:spring-boot-starter-test' 
	testImplementation 'org.apache.httpcomponents:httpclient:4.5.9' 
	providedRuntime 'com.h2database:h2:1.4.199' 
	//implementation fileTree(dir: 'libs', include: ['*.jar'])
