/*
 * Copyright 2020 The Data-Portability Project Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.datatransferproject.transfer.neil.videos;

import com.google.common.base.Preconditions;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.datatransferproject.api.launcher.Monitor;
import org.datatransferproject.spi.transfer.provider.ExportResult;
import org.datatransferproject.spi.transfer.provider.Exporter;
import org.datatransferproject.spi.transfer.types.CopyExceptionWithFailureReason;
import org.datatransferproject.transfer.neil.common.NeilClient;
import org.datatransferproject.transfer.neil.common.NeilClientFactory;
import org.datatransferproject.transfer.neil.common.NeilMediaExport;
import org.datatransferproject.types.common.ExportInformation;
import org.datatransferproject.types.common.models.videos.VideoAlbum;
import org.datatransferproject.types.common.models.videos.VideoObject;
import org.datatransferproject.types.common.models.videos.VideosContainerResource;
import org.datatransferproject.types.transfer.auth.TokensAndUrlAuthData;

public class NeilVideosExporter
    implements Exporter<TokensAndUrlAuthData, VideosContainerResource> {

  private final Monitor monitor;

  private final NeilClientFactory neilClientFactory;

  public NeilVideosExporter(NeilClientFactory neilClientFactory, Monitor monitor) {
    this.neilClientFactory = neilClientFactory;
    this.monitor = monitor;
  }

  @Override
  public ExportResult<VideosContainerResource> export(
      UUID jobId, TokensAndUrlAuthData authData, Optional<ExportInformation> exportInformation)
      throws CopyExceptionWithFailureReason {
    Preconditions.checkNotNull(authData);

    NeilClient neilClient = neilClientFactory.create(authData);
    NeilMediaExport export = new NeilMediaExport(neilClient, monitor);

    try {
      export.export();

      List<VideoAlbum> exportAlbums = export.getVideoAlbums();
      List<VideoObject> exportVideos = export.getVideos();

      VideosContainerResource containerResource =
          new VideosContainerResource(exportAlbums, exportVideos);

      return new ExportResult<>(ExportResult.ResultType.END, containerResource, null);
    } catch (IOException e) {
      return new ExportResult<>(e);
    }
  }
}
