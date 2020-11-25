/**
 * 
 */


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.errors.IncorrectObjectTypeException;
import org.eclipse.jgit.errors.MissingObjectException;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.refactoringminer.api.GitService;
import org.refactoringminer.util.GitServiceImpl;

import com.google.common.collect.Lists;

/**
 * @author George Digkas <digasgeo@gmail.com>
 *
 */
//@SpringBootApplication
//@Configuration
//@EnableAutoConfiguration
//@EnableConfigurationProperties
public class ICCS3 {

	private static final String GIT_OWNER = "apache";
	private static final String GIT_REPO = "commons-io";

	private static final String LANGUAGE = "java";
	private static final String JAVA_FILE = ".java";

	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		//		ApplicationContext ctx = SpringApplication.run(ICCS1.class, args);

		GitService gitService = new GitServiceImpl();
		Repository repo = gitService.cloneIfNotExists("tmp/" + GIT_REPO, "https://github.com/" + GIT_OWNER +"/"+ GIT_REPO);

		GitServiceBean gitServiceBean = new GitServiceBean();
		gitServiceBean.setGit(Git.open(new File("tmp/" + GIT_REPO)));
		gitServiceBean.setDirectory(new File("tmp/" + GIT_REPO));

		List<RevCommit> revCommits = null;
		try {
			revCommits = Lists.newArrayList(gitServiceBean.getGit().log().call());

			Git git = gitServiceBean.getGit();
			Project gitProject;

			Project project = null;
			if (Objects.nonNull(project)) {
				gitProject = project;
			}
			else {
				Project createdProject = new Project(GIT_REPO, GIT_OWNER+":"+GIT_REPO);
				gitProject = createdProject;
			}

			Events lastAnalyzedEvent = null;
			Map<String, Commit> alreadyPersistedCommitsMap = new HashMap<>();

			List<RevCommit> revCommitsToBePersisted = revCommits.stream().filter(revCommit -> Objects.isNull(alreadyPersistedCommitsMap.get(revCommit.getName()))).collect(Collectors.toList());

			//Dijkstra Longest Path
			DijkstraLongestPath dlp = new DijkstraLongestPath(revCommits);
			List<RevCommit> candidateRevCommitsForAnalysis = dlp.getDijkstraLongestPathAsRevCommitsList();

			//			filterRevCommits.setGitServiceBean(gitServiceBean).setGit(git).setRevCommitsAndRevCommitsMap(candidateRevCommitsForAnalysis);
			//			candidateRevCommitsForAnalysis = filterRevCommits.getFilteredRevCommits();
			///////////////////////////////////////////////////////////////////////////////////////////////////////

			CanonicalTreeParser newCanonicalTreeParser;
			CanonicalTreeParser oldCanonicalTreeParser;
			List<DiffEntry> diffEntries;
			List<RevCommit> filteredRevCommits = new ArrayList<>();

			revCommits = candidateRevCommitsForAnalysis;
			Map<String, RevCommit> revCommitsMap = revCommits.stream().collect(Collectors.toMap(RevCommit::getName, Function.identity()));

			boolean doesTheFirstCommitContainsFiles = false;
			Path path = Paths.get(gitServiceBean.getDirectory().getAbsolutePath());
			for (String extension : Stream.of(JAVA_FILE).collect(Collectors.toSet())) {
				try (Stream<Path> paths = Files.walk(path)) {
					if (!paths.filter(Files::isRegularFile).filter(p -> p.toAbsolutePath().toString().endsWith(extension)).collect(Collectors.toList()).isEmpty())
						doesTheFirstCommitContainsFiles = true;
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}

			RevCommit firstRevCommit = null;
			if (doesTheFirstCommitContainsFiles)
				filteredRevCommits.add(firstRevCommit );
			revCommits.remove(firstRevCommit);

			for (RevCommit revCommit : revCommits) {
				gitServiceBean.gitCheckout(revCommit);
				boolean pomXmlExists = Paths.get(gitServiceBean.getDirectory().getAbsolutePath() + File.separator +"pom.xml").toFile().exists();
				if (pomXmlExists) {
					//FIXME Now I think only checks if the 1st commit has no parents. Maybe I should check if the edge to the first commit exists
					RevCommit oldRevCommitParent = null;
					for (RevCommit inRevCommit : revCommit.getParents()) {
						RevCommit rc = revCommitsMap.get(inRevCommit.getName());
						if (Objects.nonNull(rc))
							oldRevCommitParent = rc;
					}
					if (revCommit.getParents().length > 0) {
						//						oldCanonicalTreeParser = getCanonicalTreeParserFromRevCommit(git.getRepository(), oldRevCommitParent);
						oldCanonicalTreeParser = null;
						Repository repository = git.getRepository();
						try (RevWalk walk = new RevWalk(repository)) {
							RevCommit commit = walk.parseCommit(oldRevCommitParent);
							ObjectId treeId = commit.getTree().getId();
							try (ObjectReader reader = repository.newObjectReader()) {
								oldCanonicalTreeParser = new CanonicalTreeParser(null, reader, treeId);
							}
						} catch (MissingObjectException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IncorrectObjectTypeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						//						newCanonicalTreeParser = getCanonicalTreeParserFromRevCommit(git.getRepository(), revCommit);
						newCanonicalTreeParser = null;
						try (RevWalk walk = new RevWalk(repository)) {
							RevCommit commit = walk.parseCommit(revCommit);
							ObjectId treeId = commit.getTree().getId();
							try (ObjectReader reader = repository.newObjectReader()) {
								newCanonicalTreeParser = new CanonicalTreeParser(null, reader, treeId);
							}
						} catch (MissingObjectException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IncorrectObjectTypeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}

						diffEntries = null;

						try {
							diffEntries = git.diff().setOldTree(oldCanonicalTreeParser).setNewTree(newCanonicalTreeParser).setShowNameAndStatusOnly(true).call();
						} catch (GitAPIException e) {
							e.printStackTrace();
						}

						boolean diffEntriesContainFiles = false;
						for (String extension : Stream.of(JAVA_FILE).collect(Collectors.toSet()))
							if (!diffEntries.stream().filter(diffEntry -> diffEntry.getNewPath().endsWith(extension) || diffEntry.getOldPath().endsWith(extension)).collect(Collectors.toList()).isEmpty())
								diffEntriesContainFiles = true;

						if (diffEntriesContainFiles)
							filteredRevCommits.add(revCommit);
					}
					else if (revCommit.getParents().length == 0)
						filteredRevCommits.add(revCommit);
				}
			}

			candidateRevCommitsForAnalysis = filteredRevCommits;

			candidateRevCommitsForAnalysis = candidateRevCommitsForAnalysis.stream().filter(Objects::nonNull).sorted(Comparator.comparingInt(RevCommit::getCommitTime)).collect(Collectors.toList());
			//candidateRevCommitsForAnalysis = filterOutRevCommitsWithTheSameCommitTime(candidateRevCommitsForAnalysis);

			List<RevCommit> revCommitsNew = new ArrayList<>();
			for (int i = 0; i < candidateRevCommitsForAnalysis.size() - 1; i++)
				if (candidateRevCommitsForAnalysis.get(i).getCommitTime() < candidateRevCommitsForAnalysis.get(i + 1).getCommitTime())
					revCommitsNew.add(candidateRevCommitsForAnalysis.get(i));
			candidateRevCommitsForAnalysis = revCommitsNew;


			List<RevCommit> revCommitsForAnalysis;
			if (Objects.nonNull(lastAnalyzedEvent))
				for (int i = 0; i < candidateRevCommitsForAnalysis.size(); i++)
					if (Objects.equals(candidateRevCommitsForAnalysis.get(i).getName(), lastAnalyzedEvent.getName()))
						revCommitsForAnalysis = candidateRevCommitsForAnalysis.subList(i, candidateRevCommitsForAnalysis.size());
			revCommitsForAnalysis = candidateRevCommitsForAnalysis;

			//Checks if the last analyzed commit is parent one of the new commits
			if ((revCommitsForAnalysis.size() == candidateRevCommitsForAnalysis.size()) && Objects.nonNull(lastAnalyzedEvent))
				revCommits = new ArrayList<>();
			else
				revCommits = revCommitsForAnalysis;

		} catch (GitAPIException e) {
			e.printStackTrace();
		}
		System.out.println(revCommits.size());
	}

	private static List<RevCommit> filterRevCommitStartingPoint(List<RevCommit> rcs, RevCommit startRevCommit) {
		for (int i = 0; i < rcs.size(); i++)
			if (Objects.equals(startRevCommit.getName(), rcs.get(i).getName()))
				rcs = rcs.subList(i + 1, rcs.size());

		return rcs;
	}

}
