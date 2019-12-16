package eu.europa.esig.dss.validation.executor.timestamp;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import eu.europa.esig.dss.detailedreport.DetailedReport;
import eu.europa.esig.dss.detailedreport.jaxb.XmlBasicBuildingBlocks;
import eu.europa.esig.dss.detailedreport.jaxb.XmlName;
import eu.europa.esig.dss.diagnostic.CertificateWrapper;
import eu.europa.esig.dss.diagnostic.DiagnosticData;
import eu.europa.esig.dss.diagnostic.TimestampWrapper;
import eu.europa.esig.dss.enumerations.TimestampQualification;
import eu.europa.esig.dss.policy.ValidationPolicy;
import eu.europa.esig.dss.simplereport.jaxb.XmlCertificate;
import eu.europa.esig.dss.simplereport.jaxb.XmlCertificateChain;
import eu.europa.esig.dss.simplereport.jaxb.XmlSimpleReport;
import eu.europa.esig.dss.simplereport.jaxb.XmlTimestamp;
import eu.europa.esig.dss.simplereport.jaxb.XmlTimestampLevel;
import eu.europa.esig.dss.utils.Utils;
import eu.europa.esig.dss.validation.executor.AbstractSimpleReportBuilder;

public class SimpleReportForTimestampBuilder extends AbstractSimpleReportBuilder {
	
	public SimpleReportForTimestampBuilder(DiagnosticData diagnosticData, DetailedReport detailedReport, Date currentTime, ValidationPolicy policy) {
		super(currentTime, policy, diagnosticData, detailedReport);
	}

	@Override
	public XmlSimpleReport build() {
		XmlSimpleReport xmlSimpleReport = super.build();
		addTimestamps(xmlSimpleReport);
		return xmlSimpleReport;
	}
	
	private void addTimestamps(XmlSimpleReport report) {
		List<TimestampWrapper> timestamps = diagnosticData.getTimestampList();
		if (Utils.isCollectionNotEmpty(timestamps)) {
			for (TimestampWrapper timestampWrapper : timestamps) {
				report.getSignatureOrTimestamp().add(getXmlTimestamp(timestampWrapper));
			}
		}
	}
	
	private XmlTimestamp getXmlTimestamp(TimestampWrapper timestampWrapper) {
		XmlTimestamp xmlTimestamp = new XmlTimestamp();
		xmlTimestamp.setId(timestampWrapper.getId());
		xmlTimestamp.setProductionTime(timestampWrapper.getProductionTime());
		xmlTimestamp.setProducedBy(getProducedByName(timestampWrapper));
		xmlTimestamp.setCertificateChain(getCertificateChain(timestampWrapper));
		xmlTimestamp.setFilename(timestampWrapper.getFilename());
		
		XmlBasicBuildingBlocks timestampBBB = detailedReport.getBasicBuildingBlockById(timestampWrapper.getId());
		xmlTimestamp.setIndication(timestampBBB.getConclusion().getIndication());
		xmlTimestamp.setSubIndication(timestampBBB.getConclusion().getSubIndication());
		xmlTimestamp.getErrors().addAll(toStrings(timestampBBB.getConclusion().getErrors()));
		xmlTimestamp.getWarnings().addAll(toStrings(timestampBBB.getConclusion().getWarnings()));
		xmlTimestamp.getInfos().addAll(toStrings(timestampBBB.getConclusion().getInfos()));

		TimestampQualification timestampQualification = detailedReport.getTimestampQualification(timestampWrapper.getId());
		if (timestampQualification != null) {
			XmlTimestampLevel xmlTimestampLevel = new XmlTimestampLevel();
			xmlTimestampLevel.setValue(timestampQualification);
			xmlTimestampLevel.setDescription(timestampQualification.getLabel());
			xmlTimestamp.setTimestampLevel(xmlTimestampLevel);
		}
		
		return xmlTimestamp;
	}
	
	private String getProducedByName(TimestampWrapper timestampWrapper) {
		CertificateWrapper signingCertificate = timestampWrapper.getSigningCertificate();
		if (signingCertificate != null) {
			return signingCertificate.getReadableCertificateName();
		}
		return Utils.EMPTY_STRING;
	}
	
	private XmlCertificateChain getCertificateChain(TimestampWrapper timestampWrapper) {
		XmlCertificateChain xmlCertificateChain = new XmlCertificateChain();
		List<CertificateWrapper> certificateChain = timestampWrapper.getCertificateChain();
		if (Utils.isCollectionNotEmpty(certificateChain)) {
			for (CertificateWrapper cert : certificateChain) {
				XmlCertificate certificate = new XmlCertificate();
				certificate.setId(cert.getId());
				certificate.setQualifiedName(cert.getReadableCertificateName());
				xmlCertificateChain.getCertificate().add(certificate);
			}
		}
		return xmlCertificateChain;
	}
	
	private List<String> toStrings(List<XmlName> xmlNames) {
		List<String> strings = new ArrayList<String>();
		if (Utils.isCollectionNotEmpty(xmlNames)) {
			for (XmlName name : xmlNames) {
				strings.add(name.getValue());
			}
		}
		return strings;
	}

}
